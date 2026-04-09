package com.adminplus.service;

import com.adminplus.pojo.dto.query.LogQuery;
import com.adminplus.pojo.dto.response.LogPageResponse;
import com.adminplus.pojo.dto.response.PageResultResponse;
import com.adminplus.service.impl.LogExportServiceImpl;
import com.adminplus.utils.DictUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * LogExportService 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LogExportService Unit Tests")
class LogExportServiceTest {

    @Mock
    private LogService logService;

    @Mock
    private DictUtils dictUtils;

    @InjectMocks
    private LogExportServiceImpl logExportService;

    private LogPageResponse testLog;
    private LogQuery query;

    @BeforeEach
    void setUp() {
        testLog = new LogPageResponse(
                "log-001",
                "testuser",
                "用户管理",
                1, // OPERATION
                1, // QUERY
                "查询用户列表",
                "UserService.list",
                "params",
                "192.168.1.1",
                "本地",
                100L,
                1, // SUCCESS
                null,
                Instant.now()
        );

        query = new LogQuery(1, 10, null, null, null, null, null, null, null);

        // Mock dictUtils for dictionary label translation
        lenient().when(dictUtils.getDictLabel(anyString(), anyString())).thenAnswer(inv -> inv.getArgument(1));
    }

    @Nested
    @DisplayName("exportToExcel Tests")
    class ExportToExcelTests {

        @Test
        @DisplayName("should export logs to Excel")
        void exportToExcel_ShouldReturnExcelFile() throws IOException {
            // Given
            PageResultResponse<LogPageResponse> pageResult = new PageResultResponse<>(
                    List.of(testLog), 1L, 1, 10
            );
            when(logService.getLogList(any(LogQuery.class))).thenReturn(pageResult);

            // When
            ResponseEntity<byte[]> result = logExportService.exportToExcel(query);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().length).isGreaterThan(0);
            assertThat(result.getHeaders().getContentType().toString())
                    .contains("application/octet-stream");
        }

        @Test
        @DisplayName("should export empty Excel when no logs")
        void exportToExcel_WhenNoLogs_ShouldReturnEmptyExcel() throws IOException {
            // Given
            PageResultResponse<LogPageResponse> pageResult = new PageResultResponse<>(
                    List.of(), 0L, 1, 10
            );
            when(logService.getLogList(any(LogQuery.class))).thenReturn(pageResult);

            // When
            ResponseEntity<byte[]> result = logExportService.exportToExcel(query);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getBody()).isNotNull();
        }

        @Test
        @DisplayName("should limit export to 10000 records")
        void exportToExcel_ShouldLimitRecords() throws IOException {
            // Given
            PageResultResponse<LogPageResponse> pageResult = new PageResultResponse<>(
                    List.of(testLog), 1L, 1, 10
            );
            when(logService.getLogList(any(LogQuery.class))).thenReturn(pageResult);

            // When
            logExportService.exportToExcel(query);

            // Then
            verify(logService).getLogList(argThat(q -> q.getSize() == 10000));
        }
    }

    @Nested
    @DisplayName("exportToCsv Tests")
    class ExportToCsvTests {

        @Test
        @DisplayName("should export logs to CSV")
        void exportToCsv_ShouldReturnCsvFile() throws IOException {
            // Given
            PageResultResponse<LogPageResponse> pageResult = new PageResultResponse<>(
                    List.of(testLog), 1L, 1, 10
            );
            when(logService.getLogList(any(LogQuery.class))).thenReturn(pageResult);

            // When
            ResponseEntity<byte[]> result = logExportService.exportToCsv(query);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getHeaders().getContentType().toString())
                    .contains("text/csv");
        }

        @Test
        @DisplayName("should export empty CSV when no logs")
        void exportToCsv_WhenNoLogs_ShouldReturnEmptyCsv() throws IOException {
            // Given
            PageResultResponse<LogPageResponse> pageResult = new PageResultResponse<>(
                    List.of(), 0L, 1, 10
            );
            when(logService.getLogList(any(LogQuery.class))).thenReturn(pageResult);

            // When
            ResponseEntity<byte[]> result = logExportService.exportToCsv(query);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getBody()).isNotNull();
        }

        @Test
        @DisplayName("should handle log with null values")
        void exportToCsv_WithNullValues_ShouldHandleGracefully() throws IOException {
            // Given
            LogPageResponse logWithNulls = new LogPageResponse(
                    "log-002", null, null, null, null, null, null, null, null, null, null, null, null, null
            );
            PageResultResponse<LogPageResponse> pageResult = new PageResultResponse<>(
                    List.of(logWithNulls), 1L, 1, 10
            );
            when(logService.getLogList(any(LogQuery.class))).thenReturn(pageResult);

            // When
            ResponseEntity<byte[]> result = logExportService.exportToCsv(query);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getBody()).isNotNull();
        }
    }
}