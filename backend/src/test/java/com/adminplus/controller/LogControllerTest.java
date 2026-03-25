package com.adminplus.controller;

import com.adminplus.pojo.dto.req.LogQueryDTO;
import com.adminplus.pojo.dto.resp.LogPageVO;
import com.adminplus.pojo.dto.resp.LogStatisticsResp;
import com.adminplus.pojo.dto.resp.PageResultResp;
import com.adminplus.service.LogExportService;
import com.adminplus.service.LogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * LogController 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LogController Unit Tests")
class LogControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LogService logService;

    @Mock
    private LogExportService logExportService;

    @InjectMocks
    private LogController logController;

    private LogPageVO testLog;
    private LogStatisticsResp testStatistics;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(logController).build();
        testLog = new LogPageVO(
                "log-001", "testuser", "用户管理", 1, 1,
                "查询用户列表", "UserService.list", "{}",
                "192.168.1.1", "本地", 100L, 1, null, Instant.now()
        );
        testStatistics = new LogStatisticsResp(
                100L, 80L, 10L, 10L, 5L,
                95L, 5L, Map.of(), Map.of(), Map.of(), Map.of()
        );
    }

    @Nested
    @DisplayName("getLogList Tests")
    class GetLogListTests {

        @Test
        @DisplayName("should return log list")
        void getLogList_ShouldReturnLogList() throws Exception {
            // Given
            PageResultResp<LogPageVO> pageResult = new PageResultResp<>(List.of(testLog), 1L, 1, 10);
            when(logService.findPage(any(LogQueryDTO.class))).thenReturn(pageResult);

            // When & Then
            mockMvc.perform(get("/v1/sys/logs")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records[0].username").value("testuser"));

            verify(logService).findPage(any(LogQueryDTO.class));
        }
    }

    @Nested
    @DisplayName("getLogById Tests")
    class GetLogByIdTests {

        @Test
        @DisplayName("should return log by id")
        void getLogById_ShouldReturnLog() throws Exception {
            // Given
            when(logService.findById("log-001")).thenReturn(testLog);

            // When & Then
            mockMvc.perform(get("/v1/sys/logs/log-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.username").value("testuser"));

            verify(logService).findById("log-001");
        }
    }

    @Nested
    @DisplayName("deleteLog Tests")
    class DeleteLogTests {

        @Test
        @DisplayName("should delete log")
        void deleteLog_ShouldDeleteLog() throws Exception {
            // When & Then
            mockMvc.perform(delete("/v1/sys/logs/log-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(logService).deleteById("log-001");
        }
    }

    @Nested
    @DisplayName("deleteLogsBatch Tests")
    class DeleteLogsBatchTests {

        @Test
        @DisplayName("should delete logs batch")
        void deleteLogsBatch_ShouldDeleteLogs() throws Exception {
            // When & Then
            mockMvc.perform(delete("/v1/sys/logs/batch")
                            .contentType("application/json")
                            .content("[\"log-001\", \"log-002\"]"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(logService).deleteByIds(List.of("log-001", "log-002"));
        }
    }

    @Nested
    @DisplayName("getStatistics Tests")
    class GetStatisticsTests {

        @Test
        @DisplayName("should return statistics")
        void getStatistics_ShouldReturnStatistics() throws Exception {
            // Given
            when(logService.getStatistics()).thenReturn(testStatistics);

            // When & Then
            mockMvc.perform(get("/v1/sys/logs/statistics"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.totalCount").value(100));

            verify(logService).getStatistics();
        }
    }

    @Nested
    @DisplayName("cleanupExpiredLogs Tests")
    class CleanupExpiredLogsTests {

        @Test
        @DisplayName("should cleanup expired logs")
        void cleanupExpiredLogs_ShouldCleanup() throws Exception {
            // Given
            when(logService.cleanupExpiredLogs()).thenReturn(5);

            // When & Then
            mockMvc.perform(post("/v1/sys/logs/cleanup"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(5));

            verify(logService).cleanupExpiredLogs();
        }
    }
}