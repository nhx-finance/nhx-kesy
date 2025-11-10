package com.javaguy.nhx.service.storage;

import com.javaguy.nhx.exception.StorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Profile({"dev", "local"})
@RequiredArgsConstructor
@Slf4j
public class LocalFileStorageService implements DocumentStorageService {

    @Value("${storage.local.base-path}")
    private String basePath;

    @Override
    public String store(String folder, MultipartFile file) throws StorageException {
        try {
            String safeFolder = folder == null ? "" : folder.replace("..", "");
            Path targetDir = Paths.get(basePath, safeFolder);
            Files.createDirectories(targetDir);
            String filename = UUID.randomUUID() + "_" + sanitize(file.getOriginalFilename());
            Path target = targetDir.resolve(filename);
            Files.write(target, file.getBytes());
            log.info("Stored file locally at {}", target);
            return target.toAbsolutePath().toString();
        } catch (IOException e) {
            log.error("Failed to store file locally: {}", e.getMessage(), e);
            throw new StorageException("Failed to store file locally", e);
        }
    }

    private String sanitize(String name) {
        if (name == null) return "file";
        return name.replaceAll("[^a-zA-Z0-9_.-]", "_");
    }
}
