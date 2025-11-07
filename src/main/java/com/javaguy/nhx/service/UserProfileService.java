package com.javaguy.nhx.service;

import com.javaguy.nhx.exception.ResourceNotFoundException;
import com.javaguy.nhx.model.dto.request.UserProfileRequest;
import com.javaguy.nhx.model.dto.response.UserProfileResponse;
import com.javaguy.nhx.model.entity.User;
import com.javaguy.nhx.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;

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
            throw new IllegalArgumentException("dateOfBirth is required");
        }
        int years = Period.between(dob, LocalDate.now()).getYears();
        if (years < 18) {
            throw new IllegalArgumentException("User must be at least 18 years old");
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
