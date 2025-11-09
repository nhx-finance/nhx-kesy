package com.javaguy.nhx.model.dto.request;

import com.javaguy.nhx.model.enums.MintStatus;
import lombok.Data;

@Data
public class UpdateMintStatusRequest {
    private MintStatus status;
    private String notes;
}
