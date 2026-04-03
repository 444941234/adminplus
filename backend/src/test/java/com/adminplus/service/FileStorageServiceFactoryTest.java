package com.adminplus.service;

import com.adminplus.common.config.FileStorageConfig;
import com.adminplus.constants.StorageType;
import com.adminplus.service.impl.FileStorageServiceFactory;
import com.adminplus.service.impl.LocalFileStorageServiceImpl;
import com.adminplus.service.impl.MinioFileStorageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * FileStorageServiceFactory 测试类
 *
 * @author AdminPlus
 * @since 2026-04-03
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FileStorageServiceFactory Unit Tests")
class FileStorageServiceFactoryTest {

    @Mock
    private FileStorageConfig fileStorageConfig;

    @Mock
    private LocalFileStorageServiceImpl localFileStorageService;

    @Mock
    private MinioFileStorageServiceImpl minioFileStorageService;

    @InjectMocks
    private FileStorageServiceFactory factory;

    @Nested
    @DisplayName("fileStorageService Bean Tests")
    class FileStorageServiceBeanTests {

        @Test
        @DisplayName("should return MinIO service when configured and available")
        void shouldReturnMinioService_WhenConfiguredAndAvailable() {
            // Given
            when(fileStorageConfig.getType()).thenReturn(StorageType.MINIO);
            when(minioFileStorageService.isAvailable()).thenReturn(true);

            // When
            FileStorageService result = factory.fileStorageService(
                    localFileStorageService, minioFileStorageService);

            // Then
            assertThat(result).isEqualTo(minioFileStorageService);
        }

        @Test
        @DisplayName("should fallback to local storage when MinIO not available")
        void shouldFallbackToLocal_WhenMinioNotAvailable() {
            // Given
            when(fileStorageConfig.getType()).thenReturn(StorageType.MINIO);
            when(minioFileStorageService.isAvailable()).thenReturn(false);

            // When
            FileStorageService result = factory.fileStorageService(
                    localFileStorageService, minioFileStorageService);

            // Then
            assertThat(result).isEqualTo(localFileStorageService);
        }

        @Test
        @DisplayName("should fallback to local storage when MinIO throws exception")
        void shouldFallbackToLocal_WhenMinioThrowsException() {
            // Given
            when(fileStorageConfig.getType()).thenReturn(StorageType.MINIO);
            when(minioFileStorageService.isAvailable()).thenThrow(new RuntimeException("Connection failed"));

            // When
            FileStorageService result = factory.fileStorageService(
                    localFileStorageService, minioFileStorageService);

            // Then
            assertThat(result).isEqualTo(localFileStorageService);
        }

        @Test
        @DisplayName("should return local storage when configured as LOCAL")
        void shouldReturnLocalStorage_WhenConfiguredAsLocal() {
            // Given
            when(fileStorageConfig.getType()).thenReturn(StorageType.LOCAL);

            // When
            FileStorageService result = factory.fileStorageService(
                    localFileStorageService, minioFileStorageService);

            // Then
            assertThat(result).isEqualTo(localFileStorageService);
            // MinIO availability should not be checked
            verify(minioFileStorageService, never()).isAvailable();
        }
    }
}