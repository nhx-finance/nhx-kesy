package com.javaguy.nhx.config;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AzureBlobConfigTest {

    @InjectMocks
    private AzureBlobConfig azureBlobConfig;

    @Mock
    private BlobContainerClient mockBlobContainerClient;
    @Mock
    private StorageSharedKeyCredential mockCredential;
    @Mock
    private BlobContainerClientBuilder mockBlobContainerClientBuilder;

    private String accountName = "testAccount";
    private String accountKey = "testKey";
    private String containerName = "testContainer";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(azureBlobConfig, "accountName", accountName);
        ReflectionTestUtils.setField(azureBlobConfig, "accountKey", accountKey);
        ReflectionTestUtils.setField(azureBlobConfig, "containerName", containerName);
    }

    @Test
    void blobContainerClient_ContainerDoesNotExist_CreatesContainer() {
        // Mock the constructor for BlobContainerClientBuilder
        try (MockedConstruction<BlobContainerClientBuilder> mockedConstruction = mockConstruction(BlobContainerClientBuilder.class, (mock, context) -> {
            when(mock.endpoint(anyString())).thenReturn(mock);
            when(mock.credential(any(StorageSharedKeyCredential.class))).thenReturn(mock);
            when(mock.containerName(anyString())).thenReturn(mock);
            when(mock.buildClient()).thenReturn(mockBlobContainerClient);
        })) {
            // Mock StorageSharedKeyCredential constructor
            try (MockedConstruction<StorageSharedKeyCredential> mockedCredentialConstruction = mockConstruction(StorageSharedKeyCredential.class)) {
                when(mockBlobContainerClient.exists()).thenReturn(false);
                doNothing().when(mockBlobContainerClient).create();

                BlobContainerClient result = azureBlobConfig.blobContainerClient();

                assertNotNull(result);
                verify(mockBlobContainerClient, times(1)).exists();
                verify(mockBlobContainerClient, times(1)).create();
                verify(mockedConstruction.constructed().get(0), times(1)).endpoint(anyString());
                verify(mockedConstruction.constructed().get(0), times(1)).credential(any(StorageSharedKeyCredential.class));
                verify(mockedConstruction.constructed().get(0), times(1)).containerName(anyString());
                verify(mockedConstruction.constructed().get(0), times(1)).buildClient();
            }
        }
    }

    @Test
    void blobContainerClient_ContainerExists_DoesNotCreateContainer() {
        // Mock the constructor for BlobContainerClientBuilder
        try (MockedConstruction<BlobContainerClientBuilder> mockedConstruction = mockConstruction(BlobContainerClientBuilder.class, (mock, context) -> {
            when(mock.endpoint(anyString())).thenReturn(mock);
            when(mock.credential(any(StorageSharedKeyCredential.class))).thenReturn(mock);
            when(mock.containerName(anyString())).thenReturn(mock);
            when(mock.buildClient()).thenReturn(mockBlobContainerClient);
        })) {
            // Mock StorageSharedKeyCredential constructor
            try (MockedConstruction<StorageSharedKeyCredential> mockedCredentialConstruction = mockConstruction(StorageSharedKeyCredential.class)) {
                when(mockBlobContainerClient.exists()).thenReturn(true);

                BlobContainerClient result = azureBlobConfig.blobContainerClient();

                assertNotNull(result);
                verify(mockBlobContainerClient, times(1)).exists();
                verify(mockBlobContainerClient, never()).create();
                verify(mockedConstruction.constructed().get(0), times(1)).endpoint(anyString());
                verify(mockedConstruction.constructed().get(0), times(1)).credential(any(StorageSharedKeyCredential.class));
                verify(mockedConstruction.constructed().get(0), times(1)).containerName(anyString());
                verify(mockedConstruction.constructed().get(0), times(1)).buildClient();
            }
        }
    }
}
