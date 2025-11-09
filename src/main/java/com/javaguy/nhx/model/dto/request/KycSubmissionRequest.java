package com.javaguy.nhx.model.dto.request;

import lombok.Data;

@Data
public class KycSubmissionRequest {
    private String fullName;
    private String dob;
    private String documentType;
    private String documentNumber;
}
