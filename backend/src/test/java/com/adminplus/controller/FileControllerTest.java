package com.adminplus.controller;

import com.adminplus.enums.StorageType;
import com.adminplus.pojo.dto.response.FileResponse;
import com.adminplus.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * FileController 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FileController Unit Tests")
class FileControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FileService fileService;

    @InjectMocks
    private FileController fileController;

    private FileResponse testFile;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(fileController).build();
        testFile = new FileResponse(
                "file-001",
                "test.jpg",
                "test.jpg",
                ".jpg",
                1024L,
                "image/jpeg",
                "uploads/test.jpg",
                StorageType.LOCAL,
                "uploads",
                1,
                "user-001",
                "user-001",
                Instant.now(),
                Instant.now()
        );
    }

    @Nested
    @DisplayName("uploadFile Tests")
    class UploadFileTests {

        @Test
        @DisplayName("should upload file")
        void uploadFile_ShouldUploadFile() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.jpg", "image/jpeg", "content".getBytes());
            when(fileService.uploadFile(any(), any())).thenReturn(testFile);

            // When & Then
            mockMvc.perform(multipart("/files/upload")
                            .file(file)
                            .param("directory", "uploads"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(fileService).uploadFile(any(), any());
        }
    }

    @Nested
    @DisplayName("deleteFile Tests")
    class DeleteFileTests {

        @Test
        @DisplayName("should delete file")
        void deleteFile_ShouldDeleteFile() throws Exception {
            // When & Then
            mockMvc.perform(delete("/files/file-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(fileService).deleteFileWithAuth("file-001");
        }
    }

    @Nested
    @DisplayName("getFile Tests")
    class GetFileTests {

        @Test
        @DisplayName("should return file info")
        void getFile_ShouldReturnFileInfo() throws Exception {
            // Given
            when(fileService.getFileWithAuth("file-001")).thenReturn(testFile);

            // When & Then
            mockMvc.perform(get("/files/file-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.fileName").value("test.jpg"));

            verify(fileService).getFileWithAuth("file-001");
        }
    }

    @Nested
    @DisplayName("getMyFiles Tests")
    class GetMyFilesTests {

        @Test
        @DisplayName("should return my files")
        void getMyFiles_ShouldReturnFiles() throws Exception {
            // Given
            when(fileService.getUserFiles()).thenReturn(List.of(testFile));

            // When & Then
            mockMvc.perform(get("/files/my"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].fileName").value("test.jpg"));

            verify(fileService).getUserFiles();
        }
    }

    @Nested
    @DisplayName("getFilesByDirectory Tests")
    class GetFilesByDirectoryTests {

        @Test
        @DisplayName("should return files by directory")
        void getFilesByDirectory_ShouldReturnFiles() throws Exception {
            // Given
            when(fileService.getFilesByDirectory("uploads")).thenReturn(List.of(testFile));

            // When & Then
            mockMvc.perform(get("/files/directory/uploads"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].fileName").value("test.jpg"));

            verify(fileService).getFilesByDirectory("uploads");
        }
    }
}
