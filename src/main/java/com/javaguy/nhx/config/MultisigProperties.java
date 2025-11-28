package com.javaguy.nhx.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "multisig")
@Data
public class MultisigProperties {
    private String accountId;
    private List<String> keyList;
    private String network;
}
