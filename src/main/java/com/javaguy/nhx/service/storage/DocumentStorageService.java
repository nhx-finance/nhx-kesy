package com.javaguy.nhx.service.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface DocumentStorageService {
    /**
     * Stores a file and returns a storage URI or absolute path reference.
     */
    String store(String folder, MultipartFile file) throws IOException;
}
