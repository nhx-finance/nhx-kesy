package com.javaguy.nhx.service.auth;

import com.javaguy.nhx.exception.custom.ConflictException;
import com.javaguy.nhx.exception.custom.KycNotVerifiedException;
import com.javaguy.nhx.exception.custom.ResourceNotFoundException;
import com.javaguy.nhx.exception.custom.ValidationException;
import com.javaguy.nhx.model.dto.request.DetailsRequest;
import com.javaguy.nhx.model.dto.request.UserProfileRequest;
import com.javaguy.nhx.model.dto.request.WalletRequest;
import com.javaguy.nhx.model.dto.response.UserProfileResponse;
import com.javaguy.nhx.model.entity.User;
import com.javaguy.nhx.model.entity.Wallet;
import com.javaguy.nhx.model.enums.KycStatus;
import com.javaguy.nhx.repository.UserRepository;
import com.javaguy.nhx.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private UserService userService;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .email("test@example.com")
                .kycStatus(KycStatus.UNVERIFIED)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void submitDetails_Success() {
        DetailsRequest request = new DetailsRequest("John", "Doe", LocalDate.of(1990, 1, 1), "USA", "CA", "PST", true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.submitDetails(userId, request);

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals(LocalDate.of(1990, 1, 1), user.getDob());
        assertTrue(user.getTermsAccepted());
    }

    @Test
    void submitDetails_UserNotFound_ThrowsResourceNotFoundException() {
        DetailsRequest request = new DetailsRequest("John", "Doe", LocalDate.of(1990, 1, 1), "USA", "CA", "PST", true);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.submitDetails(userId, request));

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void addWhitelistedWallet_Success() {
        user.setKycStatus(KycStatus.VERIFIED);
        WalletRequest request = new WalletRequest("0x123abc", "mainnet");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(walletRepository.findByUserAndWalletAddress(any(User.class), anyString())).thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> {
            Wallet wallet = invocation.getArgument(0);
            wallet.setId(UUID.randomUUID());
            return wallet;
        });

        UUID walletId = userService.addWhitelistedWallet(userId, request);

        assertNotNull(walletId);
        verify(userRepository, times(1)).findById(userId);
        verify(walletRepository, times(1)).findByUserAndWalletAddress(user, request.walletAddress());
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    void addWhitelistedWallet_UserNotFound_ThrowsResourceNotFoundException() {
        WalletRequest request = new WalletRequest("0x123abc", "mainnet");
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.addWhitelistedWallet(userId, request));

        verify(userRepository, times(1)).findById(userId);
        verify(walletRepository, never()).findByUserAndWalletAddress(any(User.class), anyString());
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void addWhitelistedWallet_KycNotVerified_ThrowsKycNotVerifiedException() {
        user.setKycStatus(KycStatus.PENDING);
        WalletRequest request = new WalletRequest("0x123abc", "mainnet");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(KycNotVerifiedException.class, () -> userService.addWhitelistedWallet(userId, request));

        verify(userRepository, times(1)).findById(userId);
        verify(walletRepository, never()).findByUserAndWalletAddress(any(User.class), anyString());
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void addWhitelistedWallet_WalletAlreadyWhitelisted_ThrowsConflictException() {
        user.setKycStatus(KycStatus.VERIFIED);
        WalletRequest request = new WalletRequest("0x123abc", "mainnet");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(walletRepository.findByUserAndWalletAddress(any(User.class), anyString()))
                .thenReturn(Optional.of(new Wallet()));

        assertThrows(ConflictException.class, () -> userService.addWhitelistedWallet(userId, request));

        verify(userRepository, times(1)).findById(userId);
        verify(walletRepository, times(1)).findByUserAndWalletAddress(user, request.walletAddress());
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void getWhitelistedWallets_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(walletRepository.findByUser(user)).thenReturn(Collections.singletonList(new Wallet()));

        List<Wallet> wallets = userService.getWhitelistedWallets(userId);

        assertNotNull(wallets);
        assertEquals(1, wallets.size());
        verify(userRepository, times(1)).findById(userId);
        verify(walletRepository, times(1)).findByUser(user);
    }

    @Test
    void getWhitelistedWallets_UserNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getWhitelistedWallets(userId));

        verify(userRepository, times(1)).findById(userId);
        verify(walletRepository, never()).findByUser(any(User.class));
    }

    @Test
    void saveProfile_Success_ValidAge() {
        UserProfileRequest request = new UserProfileRequest("John", "Doe", LocalDate.of(1990, 1, 1), "USA", "CA", "PST",
                true, "1.0");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserProfileResponse response = userService.saveProfile(userId, request);

        assertNotNull(response);
        assertTrue(response.isProfileComplete());
        assertEquals("John", user.getFirstName());
        assertEquals(LocalDate.of(1990, 1, 1), user.getDob());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void saveProfile_UserNotFound_ThrowsResourceNotFoundException() {
        UserProfileRequest request = new UserProfileRequest("John", "Doe", LocalDate.of(1990, 1, 1), "USA", "CA", "PST",
                true, "1.0");
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.saveProfile(userId, request));

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void saveProfile_DobNull_ThrowsValidationException() {
        UserProfileRequest request = new UserProfileRequest("John", "Doe", null, "USA", "CA", "PST", true, "1.0");

        assertThrows(ValidationException.class, () -> userService.saveProfile(userId, request));

        verify(userRepository, never()).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void saveProfile_Under18_ThrowsValidationException() {
        UserProfileRequest request = new UserProfileRequest("John", "Doe", LocalDate.now().minusYears(10), "USA", "CA",
                "PST", true, "1.0");

        assertThrows(ValidationException.class, () -> userService.saveProfile(userId, request));

        verify(userRepository, never()).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getProfile_Success() {
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setDob(LocalDate.of(1990, 1, 1));
        user.setCountry("USA");
        user.setProvince("CA");
        user.setTimezone("PST");
        user.setTermsAccepted(true);
        user.setTermsVersion("1.0");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserProfileResponse response = userService.getProfile(userId);

        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        assertEquals("John", response.getFirstName());
        assertTrue(response.isProfileComplete());
        assertEquals(KycStatus.UNVERIFIED, response.getKycStatus());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getProfile_UserNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getProfile(userId));

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void toResponse_ProfileNotComplete() {
        // User with some missing fields
        user.setFirstName("John");
        user.setDob(LocalDate.of(1990, 1, 1));
        user.setTermsAccepted(false); // Missing last name, country, etc.
        user.setTermsVersion(null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserProfileResponse response = userService.getProfile(userId);
        assertFalse(response.isProfileComplete());
    }

    @Test
    void toResponse_ProfileComplete() {
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setDob(LocalDate.of(1990, 1, 1));
        user.setCountry("USA");
        user.setProvince("CA");
        user.setTimezone("PST");
        user.setTermsAccepted(true);
        user.setTermsVersion("1.0");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserProfileResponse response = userService.getProfile(userId);
        assertTrue(response.isProfileComplete());
    }
}
