package com.adminplus.service;

import com.adminplus.common.properties.FileStorageProperties;
import com.adminplus.constants.StorageType;
import com.adminplus.service.impl.LocalFileStorageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * LocalFileStorageService 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LocalFileStorageService Unit Tests")
class LocalFileStorageServiceTest {

    @Mock
    private FileStorageProperties config;

    @Mock
    private FileStorageProperties.LocalConfig localConfig;

    @InjectMocks
    private LocalFileStorageServiceImpl fileStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        lenient().when(config.getLocal()).thenReturn(localConfig);
    }

    @Nested
    @DisplayName("getStorageType Tests")
    class GetStorageTypeTests {

        @Test
        @DisplayName("should return LOCAL storage type")
        void getStorageType_ShouldReturnLocal() {
            // When
            StorageType result = fileStorageService.getStorageType();

            // Then
            assertThat(result).isEqualTo(StorageType.LOCAL);
        }
    }

    @Nested
    @DisplayName("getAccessUrl Tests")
    class GetAccessUrlTests {

        @Test
        @DisplayName("should return access URL for object")
        void getAccessUrl_ShouldReturnUrl() {
            // Given
            when(localConfig.getAccessPrefix()).thenReturn("/files");

            // When
            String result = fileStorageService.getAccessUrl("uploads/test.jpg");

            // Then
            assertThat(result).isEqualTo("/files/uploads/test.jpg");
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
                    .isInstanceOf(IllegalArgumentException.class)
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
                    .isInstanceOf(IllegalArgumentException.class)
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
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("不支持的文件格式");
        }
    }

    @Nested
    @DisplayName("deleteFile Tests")
    class DeleteFileTests {

        @Test
        @DisplayName("should return false for non-existent file")
        void deleteFile_WhenFileNotExists_ShouldReturnFalse() {
            // Given
            when(localConfig.getBasePath()).thenReturn(tempDir.toString());
            when(localConfig.getAccessPrefix()).thenReturn("/files");

            // When
            boolean result = fileStorageService.deleteFile("/files/nonexistent.jpg");

            // Then
            assertThat(result).isFalse();
        }
    }
}