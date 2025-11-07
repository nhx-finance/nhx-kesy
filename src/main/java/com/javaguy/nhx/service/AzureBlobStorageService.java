package com.javaguy.nhx.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
public class AzureBlobStorageService {

    public String uploadDocument(MultipartFile file) throws IOException {
        // TODO: Integrate with azure blob
        log.info("STUB: Uploading document {} (size: {} bytes)", 
                file.getOriginalFilename(), file.getSize());
        ;
        
        return "";
    }
}
