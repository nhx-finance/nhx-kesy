package com.javaguy.nhx.service.storage;

import com.javaguy.nhx.exception.StorageException;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentStorageService {
    /**
     * Stores a file and returns a storage URI or absolute path reference.
     */
    String store(String folder, MultipartFile file) throws StorageException;
}
