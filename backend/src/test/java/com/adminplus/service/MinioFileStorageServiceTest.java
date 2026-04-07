package com.adminplus.service;

import com.adminplus.common.properties.FileStorageProperties;
import com.adminplus.common.properties.FileStorageProperties.MinioConfig;
import com.adminplus.enums.StorageType;
import com.adminplus.service.impl.MinioFileStorageServiceImpl;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * MinioFileStorageService 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MinioFileStorageService Unit Tests")
class MinioFileStorageServiceTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private FileStorageProperties config;

    @Mock
    private MinioConfig minioConfig;

    @InjectMocks
    private MinioFileStorageServiceImpl fileStorageService;

    @BeforeEach
    void setUp() {
        lenient().when(config.getMinio()).thenReturn(minioConfig);
        lenient().when(minioConfig.getBucketName()).thenReturn("adminplus");
        lenient().when(minioConfig.getAccessDomain()).thenReturn("");
        lenient().when(minioConfig.getEndpoint()).thenReturn("http://localhost:9000");
        lenient().when(minioConfig.isSecure()).thenReturn(false);
    }

    @Nested
    @DisplayName("getStorageType Tests")
    class GetStorageTypeTests {

        @Test
        @DisplayName("should return MINIO storage type")
        void getStorageType_ShouldReturnMinio() {
            // When
            StorageType result = fileStorageService.getStorageType();

            // Then
            assertThat(result).isEqualTo(StorageType.MINIO);
        }
    }

    @Nested
    @DisplayName("getAccessUrl Tests")
    class GetAccessUrlTests {

        @Test
        @DisplayName("should return access URL with custom domain")
        void getAccessUrl_WithCustomDomain_ShouldReturnUrl() {
            // Given
            when(minioConfig.getAccessDomain()).thenReturn("https://cdn.example.com");

            // When
            String result = fileStorageService.getAccessUrl("uploads/test.jpg");

            // Then
            assertThat(result).isEqualTo("https://cdn.example.com/uploads/test.jpg");
        }

        @Test
        @DisplayName("should return access URL with default endpoint")
        void getAccessUrl_WithDefaultEndpoint_ShouldReturnUrl() {
            // When
            String result = fileStorageService.getAccessUrl("uploads/test.jpg");

            // Then
            assertThat(result).isEqualTo("http://localhost:9000/adminplus/uploads/test.jpg");
        }
    }

    @Nested
    @DisplayName("uploadFile Tests")
    class UploadFileTests {

        @Test
        @DisplayName("should throw exception when file name is null")
        void uploadFile_WithNullFileName_ShouldThrowException() {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file", null, "image/jpeg", "content".getBytes());

            // When & Then
            assertThatThrownBy(() -> fileStorageService.uploadFile(file, "uploads"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("文件名不能为空");
        }

        @Test
        @DisplayName("should throw exception when file name is empty")
        void uploadFile_WithEmptyFileName_ShouldThrowException() {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file", "", "image/jpeg", "content".getBytes());

            // When & Then
            assertThatThrownBy(() -> fileStorageService.uploadFile(file, "uploads"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("文件名不能为空");
        }

        @Test
        @DisplayName("should throw exception for unsupported file format")
        void uploadFile_WithUnsupportedFormat_ShouldThrowException() {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.exe", "application/octet-stream", "content".getBytes());

            // When & Then
            assertThatThrownBy(() -> fileStorageService.uploadFile(file, "uploads"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("不支持的文件格式");
        }
    }

    @Nested
    @DisplayName("deleteFile Tests")
    class DeleteFileTests {

        @Test
        @DisplayName("should return true when file URL is treated as object name")
        void deleteFile_WithInvalidUrl_ShouldReturnTrue() {
            // Given - "invalid-url" doesn't match any pattern, so it's treated as object name
            when(minioConfig.getAccessDomain()).thenReturn("https://cdn.example.com");
            when(minioConfig.getBucketName()).thenReturn("adminplus");

            // When
            boolean result = fileStorageService.deleteFile("invalid-url");

            // Then - returns true because "invalid-url" is treated as object name
            assertThat(result).isTrue();
        }
    }
}