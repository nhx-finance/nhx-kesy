package com.javaguy.nhx.model.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KycSubmissionResponse {
    private String kycId;
    private String status;
    private String message;
}
