package com.javaguy.nhx.config;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureBlobConfig {

    @Value("${spring.cloud.azure.storage.blob.account-name}")
    private String accountName;

    @Value("${spring.cloud.azure.storage.blob.account-key}")
    private String accountKey;

    @Value("${spring.cloud.azure.storage.blob.container-name}")
    private String containerName;

    @Bean
    public BlobContainerClient blobContainerClient() {
        String endpoint = String.format("https://%s.blob.core.windows.net", accountName);

        StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);

        BlobContainerClient containerClient = new BlobContainerClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .containerName(containerName)
                .buildClient();

        if (!containerClient.exists()) {
            containerClient.create();
        }

        return containerClient;
    }
}
