package com.javaguy.nhx.service.storage;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.javaguy.nhx.exception.custom.StorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class AzureBlobStorageServiceTest {

    @Mock
    private BlobContainerClient blobContainerClient;
    @Mock
    private BlobClient blobClient;

    @InjectMocks
    private AzureBlobStorageService azureBlobStorageService;

    private String folder = "test-folder";
    private String fileName = "test-file.txt";
    private String blobUrl = "http://mockurl.com/test-folder/test-file.txt";
    private byte[] fileContent = "Hello, World!".getBytes();
    private MultipartFile mockMultipartFile;

    @BeforeEach
    void setUp() {
        mockMultipartFile = new MockMultipartFile(
                "file", fileName, "text/plain", fileContent);

        lenient().when(blobContainerClient.getBlobClient(anyString())).thenReturn(blobClient);
        lenient().when(blobClient.getBlobUrl()).thenReturn(blobUrl);
    }

    @Test
    void store_Success() throws IOException {
        doNothing().when(blobClient).upload(any(InputStream.class), anyLong(), eq(true));
        doNothing().when(blobClient).setHttpHeaders(any(BlobHttpHeaders.class));

        String resultUrl = azureBlobStorageService.store(folder, mockMultipartFile);

        assertEquals(blobUrl, resultUrl);
        verify(blobContainerClient, times(1)).getBlobClient(anyString());
        verify(blobClient, times(1)).upload(any(InputStream.class), eq((long) fileContent.length), eq(true));
        verify(blobClient, times(1)).setHttpHeaders(any(BlobHttpHeaders.class));
    }

    @Test
    void store_FileWithoutExtension_Success() throws IOException {
        MultipartFile fileWithoutExtension = new MockMultipartFile(
                "file", "noextension", "text/plain", fileContent);
        String expectedBlobNamePattern = folder + "/" + "[a-fA-F0-9\\-]+";

        when(blobContainerClient.getBlobClient(argThat(s -> s.matches(expectedBlobNamePattern))))
                .thenReturn(blobClient);
        doNothing().when(blobClient).upload(any(InputStream.class), anyLong(), eq(true));
        doNothing().when(blobClient).setHttpHeaders(any(BlobHttpHeaders.class));

        String resultUrl = azureBlobStorageService.store(folder, fileWithoutExtension);

        assertEquals(blobUrl, resultUrl);
        verify(blobContainerClient, times(1)).getBlobClient(argThat(s -> s.matches(expectedBlobNamePattern)));
        verify(blobClient, times(1)).upload(any(InputStream.class), eq((long) fileContent.length), eq(true));
        verify(blobClient, times(1)).setHttpHeaders(any(BlobHttpHeaders.class));
    }

    @Test
    void store_ExceptionDuringUpload_ThrowsStorageException() throws IOException {
        doThrow(RuntimeException.class).when(blobClient).upload(any(InputStream.class), anyLong(), eq(true));

        assertThrows(StorageException.class, () -> azureBlobStorageService.store(folder, mockMultipartFile));

        verify(blobContainerClient, times(1)).getBlobClient(anyString());
        verify(blobClient, times(1)).upload(any(InputStream.class), eq((long) fileContent.length), eq(true));
        verify(blobClient, never()).setHttpHeaders(any(BlobHttpHeaders.class));
    }

    @Test
    void delete_BlobExists_Success() {
        when(blobClient.exists()).thenReturn(true);
        doNothing().when(blobClient).delete();

        azureBlobStorageService.delete(folder, fileName);

        verify(blobContainerClient, times(1)).getBlobClient(anyString());
        verify(blobClient, times(1)).exists();
        verify(blobClient, times(1)).delete();
    }

    @Test
    void delete_BlobDoesNotExist_LogsWarningAndNoDeletion() {
        when(blobClient.exists()).thenReturn(false);

        azureBlobStorageService.delete(folder, fileName);

        verify(blobContainerClient, times(1)).getBlobClient(anyString());
        verify(blobClient, times(1)).exists();
        verify(blobClient, never()).delete();
    }

    @Test
    void download_Success() {
        when(blobClient.exists()).thenReturn(true);
        doAnswer(invocation -> {
            ByteArrayOutputStream outputStream = invocation.getArgument(0);
            outputStream.write(fileContent);
            return null;
        }).when(blobClient).downloadStream(any(ByteArrayOutputStream.class));

        byte[] downloadedBytes = azureBlobStorageService.download(folder, fileName);

        assertNotNull(downloadedBytes);
        assertArrayEquals(fileContent, downloadedBytes);
        verify(blobContainerClient, times(1)).getBlobClient(anyString());
        verify(blobClient, times(1)).exists();
        verify(blobClient, times(1)).downloadStream(any(ByteArrayOutputStream.class));
    }

    @Test
    void download_BlobDoesNotExist_ThrowsStorageException() {
        when(blobClient.exists()).thenReturn(false);

        assertThrows(StorageException.class, () -> azureBlobStorageService.download(folder, fileName));

        verify(blobContainerClient, times(1)).getBlobClient(anyString());
        verify(blobClient, times(1)).exists();
        verify(blobClient, never()).downloadStream(any(ByteArrayOutputStream.class));
    }

    @Test
    void store_NullContentType_UsesDefault() throws IOException {
        MultipartFile fileWithNullContentType = new MockMultipartFile(
                "file", fileName, null, fileContent);

        doNothing().when(blobClient).upload(any(InputStream.class), anyLong(), eq(true));
        ArgumentCaptor<BlobHttpHeaders> headersCaptor = ArgumentCaptor.forClass(BlobHttpHeaders.class);
        doNothing().when(blobClient).setHttpHeaders(headersCaptor.capture());

        azureBlobStorageService.store(folder, fileWithNullContentType);

        assertEquals("application/octet-stream", headersCaptor.getValue().getContentType());
        verify(blobClient, times(1)).setHttpHeaders(any(BlobHttpHeaders.class));
    }
}
