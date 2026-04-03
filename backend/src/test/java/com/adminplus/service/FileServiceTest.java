package com.adminplus.service;

import com.adminplus.common.config.FileStorageConfig;
import com.adminplus.common.exception.BizException;
import com.adminplus.constants.StorageType;
import com.adminplus.pojo.entity.FileEntity;
import com.adminplus.repository.FileRepository;
import com.adminplus.service.impl.FileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * FileService 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FileService Unit Tests")
class FileServiceTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private FileStorageConfig fileStorageConfig;

    @InjectMocks
    private FileServiceImpl fileService;

    private FileEntity testFile;

    @BeforeEach
    void setUp() {
        testFile = new FileEntity();
        testFile.setId("file-001");
        testFile.setOriginalName("test.jpg");
        testFile.setFileName("test");
        testFile.setFileExt(".jpg");
        testFile.setFileSize(1024L);
        testFile.setContentType("image/jpeg");
        testFile.setFileUrl("http://example.com/files/test.jpg");
        testFile.setStorageType(StorageType.LOCAL);
        testFile.setDirectory("uploads");
        testFile.setStatus(0);
        testFile.setDeleted(false);
    }

    @Nested
    @DisplayName("getFileById Tests")
    class GetFileByIdTests {

        @Test
        @DisplayName("should return file when exists")
        void getFileById_WhenExists_ShouldReturnFile() {
            // Given
            when(fileRepository.findByIdAndDeletedFalse("file-001")).thenReturn(Optional.of(testFile));

            // When
            FileEntity result = fileService.getFileById("file-001");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getOriginalName()).isEqualTo("test.jpg");
        }

        @Test
        @DisplayName("should throw exception when file not found")
        void getFileById_WhenNotFound_ShouldThrowException() {
            // Given
            when(fileRepository.findByIdAndDeletedFalse("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> fileService.getFileById("non-existent"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("文件不存在");
        }
    }

    @Nested
    @DisplayName("deleteFile Tests")
    class DeleteFileTests {

        @Test
        @DisplayName("should delete file successfully")
        void deleteFile_ShouldDeleteFile() {
            // Given
            when(fileRepository.findByIdAndDeletedFalse("file-001")).thenReturn(Optional.of(testFile));
            when(fileStorageService.deleteFile(any())).thenReturn(true);
            when(fileRepository.save(any())).thenReturn(testFile);

            // When
            boolean result = fileService.deleteFile("file-001");

            // Then
            assertThat(result).isTrue();
            verify(fileStorageService).deleteFile(testFile.getFileUrl());
            verify(fileRepository).save(any(FileEntity.class));
        }

        @Test
        @DisplayName("should throw exception when file not found")
        void deleteFile_WhenNotFound_ShouldThrowException() {
            // Given
            when(fileRepository.findByIdAndDeletedFalse("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> fileService.deleteFile("non-existent"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("文件不存在");
        }
    }

    @Nested
    @DisplayName("getFilesByDirectory Tests")
    class GetFilesByDirectoryTests {

        @Test
        @DisplayName("should return files in directory")
        void getFilesByDirectory_ShouldReturnFiles() {
            // Given
            when(fileRepository.findByDirectoryAndDeletedFalseOrderByCreateTimeDesc("uploads"))
                    .thenReturn(List.of(testFile));

            // When
            List<FileEntity> result = fileService.getFilesByDirectory("uploads");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getDirectory()).isEqualTo("uploads");
        }

        @Test
        @DisplayName("should return empty list when no files")
        void getFilesByDirectory_WhenNoFiles_ShouldReturnEmptyList() {
            // Given
            when(fileRepository.findByDirectoryAndDeletedFalseOrderByCreateTimeDesc("empty"))
                    .thenReturn(List.of());

            // When
            List<FileEntity> result = fileService.getFilesByDirectory("empty");

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("uploadFile Tests")
    class UploadFileTests {

        @Test
        @DisplayName("should throw exception for invalid file type")
        void uploadFile_WithInvalidFileType_ShouldThrowException() {
            // Given
            FileStorageConfig.LocalConfig localConfig = new FileStorageConfig.LocalConfig();
            localConfig.setMaxSize(10);
            when(fileStorageConfig.getLocal()).thenReturn(localConfig);

            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.exe", "application/octet-stream", "content".getBytes());

            // When & Then
            assertThatThrownBy(() -> fileService.uploadFile(file, "uploads"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("不支持的文件类型");
        }

        @Test
        @DisplayName("should throw exception for file size exceeding limit")
        void uploadFile_WithExceededSize_ShouldThrowException() {
            // Given
            FileStorageConfig.LocalConfig localConfig = new FileStorageConfig.LocalConfig();
            localConfig.setMaxSize(1);  // 1MB limit
            when(fileStorageConfig.getLocal()).thenReturn(localConfig);

            // Create a file larger than 1MB
            byte[] largeContent = new byte[2 * 1024 * 1024];  // 2MB
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.jpg", "image/jpeg", largeContent);

            // When & Then
            assertThatThrownBy(() -> fileService.uploadFile(file, "uploads"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("文件大小超过限制");
        }
    }
}