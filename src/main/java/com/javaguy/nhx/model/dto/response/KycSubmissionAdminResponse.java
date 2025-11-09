package com.javaguy.nhx.model.dto.response;

import com.javaguy.nhx.model.enums.KycStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class KycSubmissionAdminResponse {
    private UUID kycId;
    private UUID userId;
    private String userEmail;
    private String fullName;
    private String dob;
    private String documentType;
    private String documentNumber;
    private String sourceOfFunds;
    private String documentFrontPath;
    private String documentBackPath;
    private KycStatus status;
    private LocalDateTime submittedAt;
}
