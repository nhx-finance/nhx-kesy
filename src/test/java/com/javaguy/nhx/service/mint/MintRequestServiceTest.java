package com.javaguy.nhx.service.mint;

import com.javaguy.nhx.config.MultisigProperties;
import com.javaguy.nhx.exception.custom.InvalidMintAmountException;
import com.javaguy.nhx.exception.custom.KycNotVerifiedException;
import com.javaguy.nhx.exception.custom.ResourceNotFoundException;
import com.javaguy.nhx.exception.custom.WalletMismatchException;
import com.javaguy.nhx.model.dto.request.MintRequest;
import com.javaguy.nhx.model.dto.request.UnsignedTransactionRequest;
import com.javaguy.nhx.model.dto.response.MintResponseDto;
import com.javaguy.nhx.model.dto.response.UnsignedTransactionResponse;
import com.javaguy.nhx.model.entity.Mint;
import com.javaguy.nhx.model.entity.User;
import com.javaguy.nhx.model.entity.Wallet;
import com.javaguy.nhx.model.enums.KycStatus;
import com.javaguy.nhx.model.enums.MintStatus;
import com.javaguy.nhx.repository.MintRepository;
import com.javaguy.nhx.repository.UserRepository;
import com.javaguy.nhx.repository.WalletRepository;
import com.javaguy.nhx.service.email.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MintRequestServiceTest {

    @Mock
    private MintRepository mintRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private UnsignedTransactionService unsignedTransactionService;
    @Mock
    private MultisigProperties multisigProperties;

    @InjectMocks
    private MintRequestService mintRequestService;

    private UUID userId;
    private User user;
    private Wallet wallet;
    private UUID walletId;
    private MintRequest mintRequest;
    private Mint mint;
    private UUID mintId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        walletId = UUID.randomUUID();
        mintId = UUID.randomUUID();

        user = User.builder()
                .id(userId)
                .email("test@example.com")
                .kycStatus(KycStatus.VERIFIED)
                .createdAt(LocalDateTime.now())
                .build();

        wallet = Wallet.builder()
                .id(walletId)
                .user(user)
                .walletAddress("0xabcdef1234567890")
                .build();

        mintRequest = new MintRequest();
        mintRequest.setAmountKes(BigDecimal.valueOf(150.00));
        mintRequest.setWalletId(walletId);
        //mintRequest.setTransaction_message("Test transaction message");

        mint = Mint.builder()
                .id(mintId)
                .user(user)
                .wallet(wallet)
                .amountKes(mintRequest.getAmountKes())
                .status(MintStatus.PENDING)
                .dateInitiated(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .build();

        lenient().when(multisigProperties.getAccountId()).thenReturn("1.2.3");
        lenient().when(multisigProperties.getKeyList()).thenReturn(Arrays.asList("key1", "key2", "key3"));
    }

    @Test
    void requestMint_Success() {
        UUID mockTransactionId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(unsignedTransactionService.createUnsignedTransaction(any(UnsignedTransactionRequest.class)))
                .thenReturn(new UnsignedTransactionResponse(mockTransactionId.toString()));
        when(mintRepository.save(any(Mint.class))).thenReturn(mint);
        doNothing().when(notificationService).notifyUserOnMintStatusChange(any(User.class), any(Mint.class),
                anyString());

        var response = mintRequestService.requestMint(userId, mintRequest);

        assertNotNull(response);
        assertEquals(mintId, response.getRequestId());
        assertEquals(mockTransactionId, response.getTransactionId());
        verify(userRepository, times(1)).findById(userId);
        verify(walletRepository, times(1)).findById(walletId);
        verify(unsignedTransactionService, times(1)).createUnsignedTransaction(any(UnsignedTransactionRequest.class));
        verify(mintRepository, times(1)).save(any(Mint.class));
        verify(notificationService, times(1)).notifyUserOnMintStatusChange(any(User.class), any(Mint.class),
                anyString());
    }

    @Test
    void requestMint_UserNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> mintRequestService.requestMint(userId, mintRequest));

        verify(userRepository, times(1)).findById(userId);
        verify(walletRepository, never()).findById(any(UUID.class));
    }

    @Test
    void requestMint_KycNotVerified_ThrowsKycNotVerifiedException() {
        user.setKycStatus(KycStatus.PENDING);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(KycNotVerifiedException.class, () -> mintRequestService.requestMint(userId, mintRequest));

        verify(userRepository, times(1)).findById(userId);
        verify(walletRepository, never()).findById(any(UUID.class));
    }

    @Test
    void requestMint_BelowMinimumAmount_ThrowsInvalidMintAmountException() {
        mintRequest = new MintRequest();
        mintRequest.setAmountKes(BigDecimal.valueOf(50.00));
        mintRequest.setWalletId(walletId);
        //mintRequest.setTransaction_message("Test message");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(InvalidMintAmountException.class, () -> mintRequestService.requestMint(userId, mintRequest));

        verify(userRepository, times(1)).findById(userId);
        verify(walletRepository, never()).findById(any(UUID.class));
    }

    @Test
    void requestMint_WalletNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> mintRequestService.requestMint(userId, mintRequest));

        verify(userRepository, times(1)).findById(userId);
        verify(walletRepository, times(1)).findById(walletId);
    }

    @Test
    void requestMint_WalletMismatch_ThrowsWalletMismatchException() {
        UUID otherUserId = UUID.randomUUID();
        User otherUser = User.builder().id(otherUserId).email("other@example.com").build();
        Wallet otherUserWallet = Wallet.builder().id(walletId).user(otherUser).walletAddress("0xabcdef1234567890")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(otherUserWallet));

        assertThrows(WalletMismatchException.class, () -> mintRequestService.requestMint(userId, mintRequest));

        verify(userRepository, times(1)).findById(userId);
        verify(walletRepository, times(1)).findById(walletId);
    }

    @Test
    void getMintStatus_Success() {
        when(mintRepository.findByUserIdAndId(userId, mintId)).thenReturn(Optional.of(mint));

        var response = mintRequestService.getMintStatus(userId, mintId);

        assertNotNull(response);
        assertEquals(mintId, response.getRequestId());
        assertEquals(MintStatus.PENDING, response.getStatus());
        assertNull(response.getDateCompleted());
        verify(mintRepository, times(1)).findByUserIdAndId(userId, mintId);
    }

    @Test
    void getMintStatus_Success_CompletedStatus() {
        mint.setStatus(MintStatus.TRANSFERRED);
        when(mintRepository.findByUserIdAndId(userId, mintId)).thenReturn(Optional.of(mint));

        var response = mintRequestService.getMintStatus(userId, mintId);

        assertNotNull(response);
        assertEquals(mintId, response.getRequestId());
        assertEquals(MintStatus.TRANSFERRED, response.getStatus());
        assertNotNull(response.getDateCompleted());
        verify(mintRepository, times(1)).findByUserIdAndId(userId, mintId);
    }

    @Test
    void getMintStatus_NotFound_ThrowsResourceNotFoundException() {
        when(mintRepository.findByUserIdAndId(userId, mintId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> mintRequestService.getMintStatus(userId, mintId));

        verify(mintRepository, times(1)).findByUserIdAndId(userId, mintId);
    }

    @Test
    void getAllMintsForUser_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(mintRepository.findByUser(user)).thenReturn(Collections.singletonList(mint));

        List<MintResponseDto> mints = mintRequestService.getAllMintsForUser(userId);

        assertNotNull(mints);
        assertEquals(1, mints.size());
        assertEquals(mintId, mints.get(0).getId());
        assertEquals(mint.getAmountKes(), mints.get(0).getAmountKes());
        assertEquals(mint.getWallet().getWalletAddress(), mints.get(0).getWalletAddress());
        verify(userRepository, times(1)).findById(userId);
        verify(mintRepository, times(1)).findByUser(user);
    }

    @Test
    void getAllMintsForUser_UserNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> mintRequestService.getAllMintsForUser(userId));

        verify(userRepository, times(1)).findById(userId);
        verify(mintRepository, never()).findByUser(any(User.class));
    }
}
