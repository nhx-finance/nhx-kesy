package com.javaguy.nhx.model.dto.response;

import com.javaguy.nhx.model.enums.KycStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class UserProfileResponse {
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String country;
    private String province;
    private String timezone;
    private Boolean termsAgreed;
    private String termsVersion;
    private boolean profileComplete;
    private KycStatus kycStatus;
}
