package com.javaguy.nhx.service.admin;

import com.javaguy.nhx.exception.custom.ResourceNotFoundException;
import com.javaguy.nhx.model.dto.request.UpdateKycStatusRequest;
import com.javaguy.nhx.model.dto.request.UpdateMintStatusRequest;
import com.javaguy.nhx.model.entity.KycDocument;
import com.javaguy.nhx.model.entity.Mint;
import com.javaguy.nhx.model.entity.User;
import com.javaguy.nhx.model.entity.Wallet;
import com.javaguy.nhx.model.enums.KycStatus;
import com.javaguy.nhx.model.enums.MintStatus;
import com.javaguy.nhx.repository.KycDocumentRepository;
import com.javaguy.nhx.repository.MintRepository;
import com.javaguy.nhx.repository.UserRepository;
import com.javaguy.nhx.service.email.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private KycDocumentRepository kycDocumentRepository;
    @Mock
    private MintRepository mintRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AdminService adminService;

    private User user;
    private KycDocument kycDocument;
    private Mint mint;
    private UUID kycId;
    private UUID mintId;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        kycId = UUID.randomUUID();
        mintId = UUID.randomUUID();

        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setKycStatus(KycStatus.PENDING);

        kycDocument = new KycDocument();
        kycDocument.setId(kycId);
        kycDocument.setUser(user);
        kycDocument.setFullName("Test User");
        kycDocument.setSubmittedAt(LocalDateTime.now());

        wallet = new Wallet();
        wallet.setWalletAddress("0x123abc");

        mint = new Mint();
        mint.setId(mintId);
        mint.setUser(user);
        mint.setWallet(wallet);
        mint.setAmountKes(BigDecimal.valueOf(100));
        mint.setStatus(MintStatus.PENDING);
        mint.setDateInitiated(LocalDate.now());
    }

    @Test
    void getAllKycSubmissions_NoStatus_ReturnsAll() {
        when(kycDocumentRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(kycDocument)));

        var result = adminService.getAllKycSubmissions(null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(kycId, result.getContent().get(0).getKycId());
        verify(kycDocumentRepository, times(1)).findAll(any(Pageable.class));
        verify(kycDocumentRepository, never()).findByUser_KycStatus(any(), any());
    }

    @Test
    void getAllKycSubmissions_WithStatus_ReturnsFiltered() {
        when(kycDocumentRepository.findByUser_KycStatus(eq(KycStatus.PENDING), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(kycDocument)));

        var result = adminService.getAllKycSubmissions(KycStatus.PENDING, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(kycId, result.getContent().get(0).getKycId());
        verify(kycDocumentRepository, times(1)).findByUser_KycStatus(eq(KycStatus.PENDING), any(Pageable.class));
        verify(kycDocumentRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void updateKycStatus_Success() {
        UpdateKycStatusRequest request = new UpdateKycStatusRequest();
        request.setStatus(KycStatus.VERIFIED);

        when(kycDocumentRepository.findById(kycId)).thenReturn(Optional.of(kycDocument));
        when(userRepository.save(any(User.class))).thenReturn(user);
        doNothing().when(notificationService).notifyUserOnKycStatusChange(any(), any(), any());

        adminService.updateKycStatus(kycId, request);

        assertEquals(KycStatus.VERIFIED, user.getKycStatus());
        verify(userRepository, times(1)).save(user);
        verify(notificationService, times(1)).notifyUserOnKycStatusChange(eq(user), eq(KycStatus.VERIFIED), any());
    }

    @Test
    void updateKycStatus_NotFound_ThrowsResourceNotFoundException() {
        UpdateKycStatusRequest request = new UpdateKycStatusRequest();
        request.setStatus(KycStatus.VERIFIED);

        when(kycDocumentRepository.findById(kycId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminService.updateKycStatus(kycId, request));

        verify(userRepository, never()).save(any(User.class));
        verify(notificationService, never()).notifyUserOnKycStatusChange(any(), any(), any());
    }

    @Test
    void getAllMints_NoStatus_ReturnsAll() {
        when(mintRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(mint)));

        var result = adminService.getAllMints(null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(mintId, result.getContent().get(0).getRequestId());
        verify(mintRepository, times(1)).findAll(any(Pageable.class));
        verify(mintRepository, never()).findByStatus(any(), any());
    }

    @Test
    void getAllMints_WithStatus_ReturnsFiltered() {
        when(mintRepository.findByStatus(eq(MintStatus.PENDING), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(mint)));

        var result = adminService.getAllMints(MintStatus.PENDING, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(mintId, result.getContent().get(0).getRequestId());
        verify(mintRepository, times(1)).findByStatus(eq(MintStatus.PENDING), any(Pageable.class));
        verify(mintRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void updateMintStatus_Success() {
        UpdateMintStatusRequest request = new UpdateMintStatusRequest();
        request.setStatus(MintStatus.CONFIRMED);
        request.setNotes("Approved by admin");

        when(mintRepository.findById(mintId)).thenReturn(Optional.of(mint));
        when(mintRepository.save(any(Mint.class))).thenReturn(mint);
        doNothing().when(notificationService).notifyUserOnMintStatusChange(any(), any(), any());

        adminService.updateMintStatus(mintId, request);

        assertEquals(MintStatus.CONFIRMED, mint.getStatus());
        verify(mintRepository, times(1)).save(mint);
        verify(notificationService, times(1)).notifyUserOnMintStatusChange(eq(user), eq(mint), eq("Approved by admin"));
    }

    @Test
    void updateMintStatus_NotFound_ThrowsResourceNotFoundException() {
        UpdateMintStatusRequest request = new UpdateMintStatusRequest();
        request.setStatus(MintStatus.CONFIRMED);

        when(mintRepository.findById(mintId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminService.updateMintStatus(mintId, request));

        verify(mintRepository, never()).save(any(Mint.class));
        verify(notificationService, never()).notifyUserOnMintStatusChange(any(), any(), any());
    }
}
