package com.adminplus.service;

import com.adminplus.pojo.dto.req.LogQueryReq;
import com.adminplus.pojo.dto.resp.LogPageResp;
import com.adminplus.pojo.dto.resp.PageResultResp;
import com.adminplus.pojo.entity.LogEntity;
import com.adminplus.repository.LogRepository;
import com.adminplus.service.impl.DatabaseLogStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * DatabaseLogStorage 测试类
 *
 * @author AdminPlus
 * @since 2026-04-03
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DatabaseLogStorage Unit Tests")
class DatabaseLogStorageTest {

    @Mock
    private LogRepository logRepository;

    @InjectMocks
    private DatabaseLogStorage storage;

    private LogEntity testLog;

    @BeforeEach
    void setUp() {
        testLog = new LogEntity();
        testLog.setId("log-001");
        testLog.setUsername("testuser");
        testLog.setModule("用户管理");
        testLog.setLogType(1);
        testLog.setOperationType(1);
        testLog.setDescription("测试日志");
        testLog.setIp("127.0.0.1");
        testLog.setStatus(1);
        testLog.setCreateTime(Instant.now());
        testLog.setDeleted(false);
    }

    @Nested
    @DisplayName("save Tests")
    class SaveTests {

        @Test
        @DisplayName("should save log successfully")
        void save_ShouldReturnSavedLog() {
            // Given
            when(logRepository.save(any())).thenReturn(testLog);

            // When
            LogEntity result = storage.save(testLog);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("log-001");
            verify(logRepository).save(testLog);
        }
    }

    @Nested
    @DisplayName("saveAll Tests")
    class SaveAllTests {

        @Test
        @DisplayName("should save all logs successfully")
        void saveAll_ShouldReturnSavedLogs() {
            // Given
            List<LogEntity> logs = List.of(testLog);
            when(logRepository.saveAll(logs)).thenReturn(logs);

            // When
            List<LogEntity> result = storage.saveAll(logs);

            // Then
            assertThat(result).hasSize(1);
            verify(logRepository).saveAll(logs);
        }
    }

    @Nested
    @DisplayName("findById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("should return log when exists")
        void findById_WhenExists_ShouldReturnLog() {
            // Given
            when(logRepository.findById("log-001")).thenReturn(Optional.of(testLog));

            // When
            LogEntity result = storage.findById("log-001");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("log-001");
        }

        @Test
        @DisplayName("should return null when not found")
        void findById_WhenNotFound_ShouldReturnNull() {
            // Given
            when(logRepository.findById("non-existent")).thenReturn(Optional.empty());

            // When
            LogEntity result = storage.findById("non-existent");

            // Then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("findPage Tests")
    class FindPageTests {

        @Test
        @DisplayName("should return paginated results")
        void findPage_ShouldReturnPaginatedResults() {
            // Given
            LogQueryReq query = new LogQueryReq();
            query.setPage(1);
            query.setSize(10);
            when(logRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(testLog)));

            // When
            PageResultResp<LogPageResp> result = storage.findPage(query);

            // Then
            assertThat(result.records()).hasSize(1);
            assertThat(result.total()).isEqualTo(1);
        }

        @Test
        @DisplayName("should filter by log type")
        void findPage_ShouldFilterByLogType() {
            // Given
            LogQueryReq query = new LogQueryReq();
            query.setPage(1);
            query.setSize(10);
            query.setLogType(1);
            when(logRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(testLog)));

            // When
            PageResultResp<LogPageResp> result = storage.findPage(query);

            // Then
            assertThat(result.records()).hasSize(1);
        }

        @Test
        @DisplayName("should filter by username")
        void findPage_ShouldFilterByUsername() {
            // Given
            LogQueryReq query = new LogQueryReq();
            query.setPage(1);
            query.setSize(10);
            query.setUsername("test");
            when(logRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(testLog)));

            // When
            PageResultResp<LogPageResp> result = storage.findPage(query);

            // Then
            assertThat(result.records()).hasSize(1);
        }

        @Test
        @DisplayName("should filter by status")
        void findPage_ShouldFilterByStatus() {
            // Given
            LogQueryReq query = new LogQueryReq();
            query.setPage(1);
            query.setSize(10);
            query.setStatus(1);
            when(logRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(testLog)));

            // When
            PageResultResp<LogPageResp> result = storage.findPage(query);

            // Then
            assertThat(result.records()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("count Tests")
    class CountTests {

        @Test
        @DisplayName("should return total count")
        void count_ShouldReturnTotalCount() {
            // Given
            when(logRepository.countByDeletedFalse()).thenReturn(100L);

            // When
            Long result = storage.count();

            // Then
            assertThat(result).isEqualTo(100L);
        }
    }

    @Nested
    @DisplayName("countByCondition Tests")
    class CountByConditionTests {

        @Test
        @DisplayName("should return count by condition")
        void countByCondition_ShouldReturnCount() {
            // Given
            LogQueryReq query = new LogQueryReq();
            query.setLogType(1);
            when(logRepository.count(any(Specification.class))).thenReturn(50L);

            // When
            Long result = storage.countByCondition(query);

            // Then
            assertThat(result).isEqualTo(50L);
        }
    }

    @Nested
    @DisplayName("deleteById Tests")
    class DeleteByIdTests {

        @Test
        @DisplayName("should delete by id")
        void deleteById_ShouldDelete() {
            // Given
            doNothing().when(logRepository).deleteById("log-001");

            // When
            storage.deleteById("log-001");

            // Then
            verify(logRepository).deleteById("log-001");
        }
    }

    @Nested
    @DisplayName("deleteByIds Tests")
    class DeleteByIdsTests {

        @Test
        @DisplayName("should delete by ids and return count")
        void deleteByIds_ShouldDeleteAndReturnCount() {
            // Given
            List<String> ids = List.of("log-001", "log-002");
            LogEntity log2 = new LogEntity();
            log2.setId("log-002");
            when(logRepository.findAllById(ids)).thenReturn(List.of(testLog, log2));
            doNothing().when(logRepository).deleteAll(any());

            // When
            Integer result = storage.deleteByIds(ids);

            // Then
            assertThat(result).isEqualTo(2);
            verify(logRepository).deleteAll(any());
        }

        @Test
        @DisplayName("should return 0 when no logs found")
        void deleteByIds_WhenNoneFound_ShouldReturn0() {
            // Given
            List<String> ids = List.of("non-existent");
            when(logRepository.findAllById(ids)).thenReturn(List.of());

            // When
            Integer result = storage.deleteByIds(ids);

            // Then
            assertThat(result).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("deleteByCondition Tests")
    class DeleteByConditionTests {

        @Test
        @DisplayName("should delete by condition and return count")
        void deleteByCondition_ShouldDeleteAndReturnCount() {
            // Given
            LogQueryReq query = new LogQueryReq();
            query.setStatus(0);
            when(logRepository.findAll(any(Specification.class))).thenReturn(List.of(testLog));
            doNothing().when(logRepository).deleteAll(any());

            // When
            Integer result = storage.deleteByCondition(query);

            // Then
            assertThat(result).isEqualTo(1);
            verify(logRepository).deleteAll(any());
        }
    }

    @Nested
    @DisplayName("cleanupExpiredLogs Tests")
    class CleanupExpiredLogsTests {

        @Test
        @DisplayName("should cleanup expired logs in batches")
        void cleanupExpiredLogs_ShouldDeleteInBatches() {
            // Given
            when(logRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(testLog)))
                    .thenReturn(new PageImpl<>(List.of()));

            doNothing().when(logRepository).deleteAll(any());

            // When
            Integer result = storage.cleanupExpiredLogs(30, 100);

            // Then
            assertThat(result).isEqualTo(1);
            verify(logRepository, times(1)).deleteAll(any());
        }

        @Test
        @DisplayName("should return 0 when no expired logs")
        void cleanupExpiredLogs_WhenNoneExpired_ShouldReturn0() {
            // Given
            when(logRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            // When
            Integer result = storage.cleanupExpiredLogs(30, 100);

            // Then
            assertThat(result).isEqualTo(0);
            verify(logRepository, never()).deleteAll(any());
        }
    }

    @Nested
    @DisplayName("getStrategyName Tests")
    class GetStrategyNameTests {

        @Test
        @DisplayName("should return DATABASE")
        void getStrategyName_ShouldReturnDatabase() {
            assertThat(storage.getStrategyName()).isEqualTo("DATABASE");
        }
    }

    @Nested
    @DisplayName("isAvailable Tests")
    class IsAvailableTests {

        @Test
        @DisplayName("should always return true")
        void isAvailable_ShouldAlwaysReturnTrue() {
            assertThat(storage.isAvailable()).isTrue();
        }
    }
}