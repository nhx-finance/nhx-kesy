package com.javaguy.nhx.service.storage;

import com.javaguy.nhx.exception.custom.StorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocalFileStorageServiceTest {

    @InjectMocks
    private LocalFileStorageService localFileStorageService;

    private Path tempBasePath;
    private String folder = "test-folder";
    private String fileName = "original_file_name.txt";
    private byte[] fileContent = "Test content for the file.".getBytes();
    private MultipartFile mockMultipartFile;

    @BeforeEach
    void setUp() throws IOException {
        tempBasePath = Files.createTempDirectory("local-storage-test");
        ReflectionTestUtils.setField(localFileStorageService, "basePath", tempBasePath.toAbsolutePath().toString());

        mockMultipartFile = new MockMultipartFile(
                "file", fileName, "text/plain", fileContent);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.walk(tempBasePath)
                .sorted(java.util.Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(java.io.File::delete);
    }

    @Test
    void store_Success() {
        String storedPath = localFileStorageService.store(folder, mockMultipartFile);

        assertNotNull(storedPath);
        Path expectedFilePath = Paths.get(storedPath);
        assertTrue(Files.exists(expectedFilePath));
        assertTrue(storedPath.contains(folder));
        assertTrue(storedPath.contains(sanitizeFilename(fileName)));
        assertDoesNotThrow(() -> Files.readAllBytes(expectedFilePath));
        assertArrayEquals(fileContent, assertDoesNotThrow(() -> Files.readAllBytes(expectedFilePath)));
    }

    @Test
    void store_NullFolder_Success() {
        String storedPath = localFileStorageService.store(null, mockMultipartFile);

        assertNotNull(storedPath);
        Path expectedFilePath = Paths.get(storedPath);
        assertTrue(Files.exists(expectedFilePath));
        assertFalse(storedPath.contains(folder)); // Should not contain "test-folder"
        assertTrue(storedPath.contains(sanitizeFilename(fileName)));
    }

    @Test
    void store_EmptyFolder_Success() {
        String storedPath = localFileStorageService.store("", mockMultipartFile);

        assertNotNull(storedPath);
        Path expectedFilePath = Paths.get(storedPath);
        assertTrue(Files.exists(expectedFilePath));
        assertFalse(storedPath.contains(folder)); // Should not contain "test-folder"
        assertTrue(storedPath.contains(sanitizeFilename(fileName)));
    }

    @Test
    void store_FolderWithDotDot_SanitizedSuccessfully() {
        String maliciousFolder = "malicious/../safe";
        String storedPath = localFileStorageService.store(maliciousFolder, mockMultipartFile);

        assertNotNull(storedPath);
        Path expectedFilePath = Paths.get(storedPath);
        assertTrue(Files.exists(expectedFilePath));
        // The path normalization resolves "../" so "malicious" won't be in final path
        assertTrue(storedPath.contains("safe"));
    }

    @Test
    void store_IOExceptionDuringDirectoryCreation_ThrowsStorageException() throws IOException {
        // Mock Files.createDirectories to throw IOException
        try (var mockedFiles = mockStatic(Files.class, CALLS_REAL_METHODS)) {
            mockedFiles.when(() -> Files.createDirectories(any(Path.class)))
                    .thenThrow(new IOException("Cannot create directory"));

            assertThrows(StorageException.class, () -> localFileStorageService.store(folder, mockMultipartFile));
        }
    }

    @Test
    void store_IOExceptionDuringFileWrite_ThrowsStorageException() throws IOException {
        // Mock Files.write to throw IOException
        try (var mockedFiles = mockStatic(Files.class, CALLS_REAL_METHODS)) {
            // Allow createDirectories to work but mock write
            mockedFiles.when(() -> Files.createDirectories(any(Path.class)))
                    .thenCallRealMethod();
            mockedFiles.when(() -> Files.write(any(Path.class), any(byte[].class)))
                    .thenThrow(new IOException("Cannot write file"));

            assertThrows(StorageException.class, () -> localFileStorageService.store(folder, mockMultipartFile));
        }
    }

    private String sanitizeFilename(String originalFilename) {
        if (originalFilename == null)
            return "file";
        return originalFilename.replaceAll("[^a-zA-Z0-9_.-]", "_");
    }
}
