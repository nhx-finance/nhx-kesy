package com.javaguy.nhx.service;

import com.javaguy.nhx.exception.ResourceNotFoundException;
import com.javaguy.nhx.exception.KycNotVerifiedException;
import com.javaguy.nhx.model.dto.request.DetailsRequest;
import com.javaguy.nhx.model.dto.request.WalletRequest;
import com.javaguy.nhx.model.dto.response.UserDashboardResponse;
import com.javaguy.nhx.model.entity.User;
import com.javaguy.nhx.model.entity.Wallet;
import com.javaguy.nhx.model.enums.KycStatus;
import com.javaguy.nhx.model.enums.MintStatus;
import com.javaguy.nhx.repository.UserRepository;
import com.javaguy.nhx.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    @Transactional
    public void submitDetails(UUID userId, DetailsRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setDob(request.dob());
        user.setCountry(request.country());
        user.setProvince(request.province());
        user.setTimezone(request.timezone());
        user.setTermsAccepted(request.termsAgreed());
        userRepository.save(user);

        log.info("Details submitted for user {}: {}", userId, request.firstName());
    }

    @Transactional
    public UUID addWhitelistedWallet(UUID userId, WalletRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getKycStatus() != KycStatus.VERIFIED) {
            throw new KycNotVerifiedException("User KYC must be VERIFIED to whitelist a wallet.");
        }

        if (walletRepository.findByUserAndWalletAddress(user, request.walletAddress()).isPresent()) {
            throw new IllegalArgumentException("Wallet address already whitelisted for this user.");
        }
        
        // TODO: Implement ownership verification (signature challenge or micro-transfer).

        Wallet wallet = Wallet.builder()
                .user(user)
                .walletAddress(request.walletAddress())
                .build();
        walletRepository.save(wallet);

        log.info("Wallet {} whitelisted for user {}", request.walletAddress(), userId);
        return wallet.getId();
    }

    @Transactional(readOnly = true)
    public List<Wallet> getWhitelistedWallets(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return walletRepository.findByUser(user);
    }
}
