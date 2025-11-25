package com.javaguy.nhx.service.storage;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.javaguy.nhx.exception.custom.StorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

@Service
@Profile("prod")
@RequiredArgsConstructor
@Slf4j
public class AzureBlobStorageService implements DocumentStorageService {

    private final BlobContainerClient blobContainerClient;

    @Override
    public String store(String folder, MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = (originalFilename != null && originalFilename.contains("."))
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";

            String blobName = folder + "/" + UUID.randomUUID() + extension;

            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
            blobClient.upload(file.getInputStream(), file.getSize(), true);

            String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

            blobClient.setHttpHeaders(new BlobHttpHeaders()
                    .setContentType(contentType)
                    .setContentDisposition("inline"));

            log.info("Uploaded file to Azure Blob Storage: {}", blobClient.getBlobUrl());
            return blobClient.getBlobUrl();

        } catch (Exception e) {
            log.error("Failed to upload file to Azure Blob Storage: {}", e.getMessage());
            throw new StorageException("Failed to upload file to Azure Blob Storage", e);
        }
    }

    public void delete(String folder, String fileName) {
        String blobName = folder + "/" + fileName;
        BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

        if (blobClient.exists()) {
            blobClient.delete();
            log.info("Deleted blob: {}", blobName);
        } else {
            log.warn("Blob not found for deletion: {}", blobName);
        }
    }

    public byte[] download(String folder, String fileName) {
        String blobName = folder + "/" + fileName;
        BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

        if (!blobClient.exists()) {
            log.warn("Blob not found for download: {}", blobName);
            throw new StorageException("File not found: " + fileName);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        blobClient.downloadStream(outputStream);
        log.info("Downloaded blob: {}", blobName);
        return outputStream.toByteArray();
    }
}
