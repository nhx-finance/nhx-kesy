package com.javaguy.nhx.model.dto.response;

import com.javaguy.nhx.model.enums.KycStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class KycStatusResponse {
    private KycStatus status;
    private List<String> documents;
}
