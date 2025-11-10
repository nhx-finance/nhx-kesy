package com.javaguy.nhx.service;

import com.javaguy.nhx.exception.InvalidAgeException;
import com.javaguy.nhx.exception.KycNotVerifiedException;
import com.javaguy.nhx.exception.ResourceNotFoundException;
import com.javaguy.nhx.exception.WalletAlreadyWhitelistedException;
import com.javaguy.nhx.model.dto.request.DetailsRequest;
import com.javaguy.nhx.model.dto.request.UserProfileRequest;
import com.javaguy.nhx.model.dto.request.WalletRequest;
import com.javaguy.nhx.model.dto.response.UserProfileResponse;
import com.javaguy.nhx.model.entity.User;
import com.javaguy.nhx.model.entity.Wallet;
import com.javaguy.nhx.model.enums.KycStatus;
import com.javaguy.nhx.repository.UserRepository;
import com.javaguy.nhx.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;

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
            throw new WalletAlreadyWhitelistedException("Wallet address already whitelisted for this user.");
        }

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

    @Transactional
    public UserProfileResponse saveProfile(UUID userId, UserProfileRequest request) {
        validateAge(request.dateOfBirth());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setDob(request.dateOfBirth());
        user.setCountry(request.country());
        user.setProvince(request.province());
        user.setTimezone(request.timezone());
        user.setTermsAccepted(Boolean.TRUE.equals(request.termsAgreed()));
        user.setTermsVersion(request.termsVersion());

        userRepository.save(user);

        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toResponse(user);
    }

    private void validateAge(LocalDate dob) {
        if (dob == null) {
            throw new InvalidAgeException("dateOfBirth is required");
        }
        int years = Period.between(dob, LocalDate.now()).getYears();
        if (years < 18) {
            throw new InvalidAgeException("User must be at least 18 years old");
        }
    }

    private UserProfileResponse toResponse(User user) {
        boolean profileComplete = user.getFirstName() != null && user.getLastName() != null
                && user.getDob() != null && user.getCountry() != null && user.getProvince() != null
                && user.getTimezone() != null && Boolean.TRUE.equals(user.getTermsAccepted())
                && user.getTermsVersion() != null;

        return UserProfileResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .dateOfBirth(user.getDob())
                .country(user.getCountry())
                .province(user.getProvince())
                .timezone(user.getTimezone())
                .termsAgreed(Boolean.TRUE.equals(user.getTermsAccepted()))
                .termsVersion(user.getTermsVersion())
                .profileComplete(profileComplete)
                .kycStatus(user.getKycStatus())
                .build();
    }
}
