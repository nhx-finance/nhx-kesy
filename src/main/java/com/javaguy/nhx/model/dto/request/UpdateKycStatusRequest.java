package com.javaguy.nhx.model.dto.request;

import com.javaguy.nhx.model.enums.KycStatus;
import lombok.Data;

@Data
public class UpdateKycStatusRequest {
    private KycStatus status;
    private String rejectionReason;
    private String reviewerNotes;
}
