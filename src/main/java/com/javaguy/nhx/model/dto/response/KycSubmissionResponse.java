package com.javaguy.nhx.model.dto.response;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Builder
public class KycSubmissionResponse {
    private String kycId;
    private String status;
}
