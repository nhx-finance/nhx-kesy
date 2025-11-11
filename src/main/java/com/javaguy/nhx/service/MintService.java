package com.javaguy.nhx.service;

import com.javaguy.nhx.exception.InvalidMintAmountException;
import com.javaguy.nhx.exception.KycNotVerifiedException;
import com.javaguy.nhx.exception.ResourceNotFoundException;
import com.javaguy.nhx.exception.WalletMismatchException;
import com.javaguy.nhx.model.dto.request.MintRequest;
import com.javaguy.nhx.model.dto.response.MintResponse;
import com.javaguy.nhx.model.dto.response.MintResponseDto;
import com.javaguy.nhx.model.dto.response.MintStatusResponse;
import com.javaguy.nhx.model.entity.Mint;
import com.javaguy.nhx.model.entity.User;
import com.javaguy.nhx.model.entity.Wallet;
import com.javaguy.nhx.model.enums.KycStatus;
import com.javaguy.nhx.model.enums.MintStatus;
import com.javaguy.nhx.repository.MintRepository;
import com.javaguy.nhx.repository.UserRepository;
import com.javaguy.nhx.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MintService {

    private final MintRepository mintRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final NotificationService notificationService;

    private static final BigDecimal MIN_MINT_AMOUNT = new BigDecimal("10000000.00");

    @Transactional
    public MintResponse requestMint(UUID userId, MintRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getKycStatus() != KycStatus.VERIFIED) {
            throw new KycNotVerifiedException("KYC status must be VERIFIED to initiate a mint request");
        }

        if (request.getAmountKes().compareTo(MIN_MINT_AMOUNT) < 0) {
            throw new InvalidMintAmountException("Mint amount must be at least " + MIN_MINT_AMOUNT + " KES");
        }

        Wallet wallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        if (!wallet.getUser().getId().equals(userId)) {
            throw new WalletMismatchException("Wallet does not belong to the authenticated user");
        }

        Mint mint = Mint.builder()
                .user(user)
                .wallet(wallet)
                .amountKes(request.getAmountKes())
                .status(MintStatus.PENDING)
                .dateInitiated(LocalDate.now())
                .build();

        mint = mintRepository.save(mint);

        notificationService.notifyUserOnMintStatusChange(user, mint, "Your mint request has been received and is pending.");

        return MintResponse.builder()
                .requestId(mint.getId())
                .transactionId(mint.getId())
                .build();
    }

    @Transactional(readOnly = true)
    public MintStatusResponse getMintStatus(UUID userId, UUID requestId) {
        Mint mint = mintRepository.findByUserIdAndId(userId, requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Mint request not found for this user"));

        LocalDateTime dateCompleted = null;
        if (mint.getStatus() == MintStatus.TRANSFERRED || mint.getStatus() == MintStatus.FAILED) {
            dateCompleted = mint.getCreatedAt();
        }

        return MintStatusResponse.builder()
                .requestId(mint.getId())
                .status(mint.getStatus())
                .tokensMinted(mint.getAmountKes())
                .dateInitiated(mint.getCreatedAt())
                .dateCompleted(dateCompleted)
                .build();
    }

    @Transactional(readOnly = true)
    public List<MintResponseDto> getAllMintsForUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mintRepository.findByUser(user).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private MintResponseDto convertToDto(Mint mint) {
        return MintResponseDto.builder()
                .id(mint.getId())
                .amountKes(mint.getAmountKes())
                .status(mint.getStatus())
                .dateInitiated(mint.getDateInitiated())
                .restrictionEndDate(mint.getRestrictionEndDate())
                .paymentReference(mint.getPaymentReference())
                .treasuryTransactionId(mint.getTreasuryTransactionId())
                .createdAt(mint.getCreatedAt())
                .walletAddress(mint.getWallet().getWalletAddress())
                .build();
    }
}
