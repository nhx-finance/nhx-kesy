package com.javaguy.nhx.service.kyc;

import com.javaguy.nhx.exception.custom.BadRequestException;
import com.javaguy.nhx.exception.custom.ConflictException;
import com.javaguy.nhx.exception.custom.InternalServerException;
import com.javaguy.nhx.exception.custom.ResourceNotFoundException;
import com.javaguy.nhx.model.dto.request.KycSubmissionRequest;
import com.javaguy.nhx.model.dto.response.KycSubmissionResponse;
import com.javaguy.nhx.model.entity.KycDocument;
import com.javaguy.nhx.model.entity.User;
import com.javaguy.nhx.model.enums.KycStatus;
import com.javaguy.nhx.repository.KycDocumentRepository;
import com.javaguy.nhx.repository.UserRepository;
import com.javaguy.nhx.service.email.NotificationService;
import com.javaguy.nhx.service.storage.DocumentStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KycServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private KycDocumentRepository kycDocumentRepository;
    @Mock
    private DocumentStorageService storageService;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private KycService kycService;

    private UUID userId;
    private User user;
    private KycSubmissionRequest kycSubmissionRequest;
    private MultipartFile mockDocumentFront;
    private MultipartFile mockDocumentBack;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .email("test@example.com")
                .kycStatus(KycStatus.UNVERIFIED)
                .createdAt(LocalDateTime.now())
                .build();

        kycSubmissionRequest = new KycSubmissionRequest();
        kycSubmissionRequest.setFullName("John Doe");
        kycSubmissionRequest.setDob("1990-01-01");
        kycSubmissionRequest.setDocumentType("National ID");
        kycSubmissionRequest.setDocumentNumber("123456789");

        mockDocumentFront = new MockMultipartFile(
                "documentFront", "front.jpg", MediaType.IMAGE_JPEG_VALUE, "front_content".getBytes());
        mockDocumentBack = new MockMultipartFile(
                "documentBack", "back.jpg", MediaType.IMAGE_JPEG_VALUE, "back_content".getBytes());
    }

    @Test
    void initiateKyc_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        var result = kycService.initiateKyc(userId);

        assertNotNull(result);
        assertEquals(KycStatus.INITIATED.name(), result.get("status"));
        assertEquals("KYC initiated. Proceed with document submission.", result.get("message"));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
        assertEquals(KycStatus.INITIATED, user.getKycStatus());
    }

    @Test
    void initiateKyc_UserNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> kycService.initiateKyc(userId));

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void submitDocuments_Success() {
        user.setKycStatus(KycStatus.INITIATED);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(storageService.store(anyString(), eq(mockDocumentFront))).thenReturn("path/to/front.jpg");
        when(storageService.store(anyString(), eq(mockDocumentBack))).thenReturn("path/to/back.jpg");
        when(kycDocumentRepository.save(any(KycDocument.class))).thenAnswer(invocation -> {
            KycDocument kycDoc = invocation.getArgument(0);
            kycDoc.setId(UUID.randomUUID());
            return kycDoc;
        });
        when(userRepository.save(any(User.class))).thenReturn(user);
        doNothing().when(notificationService).notifyAdminsOnKycSubmission(any(User.class));

        KycSubmissionResponse response = kycService.submitDocuments(userId, kycSubmissionRequest, mockDocumentFront,
                mockDocumentBack);

        assertNotNull(response);
        assertEquals(KycStatus.SUBMITTED.name(), response.getStatus());
        assertEquals("KYC documents submitted successfully. Your submission is under review.", response.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(storageService, times(1)).store(anyString(), eq(mockDocumentFront));
        verify(storageService, times(1)).store(anyString(), eq(mockDocumentBack));
        verify(kycDocumentRepository, times(1)).save(any(KycDocument.class));
        verify(userRepository, times(1)).save(user);
        assertEquals(KycStatus.SUBMITTED, user.getKycStatus());
        verify(notificationService, times(1)).notifyAdminsOnKycSubmission(user);
    }

    @Test
    void submitDocuments_UserNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> kycService.submitDocuments(userId, kycSubmissionRequest, mockDocumentFront, mockDocumentBack));

        verify(userRepository, times(1)).findById(userId);
        verify(storageService, never()).store(anyString(), any(MultipartFile.class));
    }

    @Test
    void submitDocuments_KycAlreadyVerified_ThrowsConflictException() {
        user.setKycStatus(KycStatus.VERIFIED);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(ConflictException.class,
                () -> kycService.submitDocuments(userId, kycSubmissionRequest, mockDocumentFront, mockDocumentBack));

        verify(userRepository, times(1)).findById(userId);
        verify(storageService, never()).store(anyString(), any(MultipartFile.class));
    }

    @Test
    void submitDocuments_DocumentFrontMissing_ThrowsBadRequestException() {
        user.setKycStatus(KycStatus.INITIATED);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class,
                () -> kycService.submitDocuments(userId, kycSubmissionRequest, null, mockDocumentBack));

        verify(userRepository, times(1)).findById(userId);
        verify(storageService, never()).store(anyString(), any(MultipartFile.class));
    }

    @Test
    void submitDocuments_DocumentBackMissing_ThrowsBadRequestException() {
        user.setKycStatus(KycStatus.INITIATED);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class,
                () -> kycService.submitDocuments(userId, kycSubmissionRequest, mockDocumentFront, null));

        verify(userRepository, times(1)).findById(userId);
        verify(storageService, never()).store(anyString(), any(MultipartFile.class));
    }

    @Test
    void submitDocuments_InvalidDocumentType_ThrowsBadRequestException() {
        user.setKycStatus(KycStatus.INITIATED);
        MultipartFile invalidFile = new MockMultipartFile(
                "documentFront", "front.txt", MediaType.TEXT_PLAIN_VALUE, "content".getBytes());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class,
                () -> kycService.submitDocuments(userId, kycSubmissionRequest, invalidFile, mockDocumentBack));

        verify(userRepository, times(1)).findById(userId);
        verify(storageService, never()).store(anyString(), any(MultipartFile.class));
    }

    @Test
    void submitDocuments_OversizedDocument_ThrowsBadRequestException() {
        user.setKycStatus(KycStatus.INITIATED);
        MultipartFile oversizedFile = new MockMultipartFile(
                "documentFront", "front.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[11 * 1024 * 1024] // 11MB
        );
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class,
                () -> kycService.submitDocuments(userId, kycSubmissionRequest, oversizedFile, mockDocumentBack));

        verify(userRepository, times(1)).findById(userId);
        verify(storageService, never()).store(anyString(), any(MultipartFile.class));
    }

    @Test
    void submitDocuments_StorageServiceFails_ThrowsInternalServerException() {
        user.setKycStatus(KycStatus.INITIATED);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(storageService.store(anyString(), eq(mockDocumentFront))).thenThrow(new RuntimeException("Storage error"));

        assertThrows(InternalServerException.class,
                () -> kycService.submitDocuments(userId, kycSubmissionRequest, mockDocumentFront, mockDocumentBack));

        verify(userRepository, times(1)).findById(userId);
        verify(storageService, times(1)).store(anyString(), eq(mockDocumentFront));
        verify(kycDocumentRepository, never()).save(any(KycDocument.class));
    }

    @Test
    void getKycStatus_UserNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> kycService.getKycStatus(userId));

        verify(userRepository, times(1)).findById(userId);
        verify(kycDocumentRepository, never()).findByUserId(any(UUID.class));
    }

    @Test
    void getKycStatus_KycInitiated_ReturnsCorrectStatus() {
        user.setKycStatus(KycStatus.INITIATED);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        var response = kycService.getKycStatus(userId);

        assertNotNull(response);
        assertEquals(KycStatus.INITIATED, response.getStatus());
        assertTrue(response.getDocuments().isEmpty());
        verify(userRepository, times(1)).findById(userId);
        verify(kycDocumentRepository, never()).findByUserId(any(UUID.class));
    }

    @Test
    void getKycStatus_KycSubmitted_ReturnsCorrectStatusWithDocuments() {
        user.setKycStatus(KycStatus.SUBMITTED);
        KycDocument kycDoc = KycDocument.builder()
                .id(UUID.randomUUID())
                .user(user)
                .documentFrontPath("path/front.jpg")
                .documentBackPath("path/back.jpg")
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(kycDocumentRepository.findByUserId(userId)).thenReturn(Collections.singletonList(kycDoc));

        var response = kycService.getKycStatus(userId);

        assertNotNull(response);
        assertEquals(KycStatus.SUBMITTED, response.getStatus());
        assertFalse(response.getDocuments().isEmpty());
        assertEquals(2, response.getDocuments().size());
        verify(userRepository, times(1)).findById(userId);
        verify(kycDocumentRepository, times(1)).findByUserId(userId);
    }

    @Test
    void getKycStatus_KycVerified_ReturnsCorrectStatusWithDocuments() {
        user.setKycStatus(KycStatus.VERIFIED);
        KycDocument kycDoc = KycDocument.builder()
                .id(UUID.randomUUID())
                .user(user)
                .documentFrontPath("path/front.jpg")
                .documentBackPath("path/back.jpg")
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(kycDocumentRepository.findByUserId(userId)).thenReturn(Collections.singletonList(kycDoc));

        var response = kycService.getKycStatus(userId);

        assertNotNull(response);
        assertEquals(KycStatus.VERIFIED, response.getStatus());
        assertFalse(response.getDocuments().isEmpty());
        assertEquals(2, response.getDocuments().size());
        verify(userRepository, times(1)).findById(userId);
        verify(kycDocumentRepository, times(1)).findByUserId(userId);
    }
}
