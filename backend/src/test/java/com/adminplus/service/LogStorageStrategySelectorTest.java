package com.adminplus.service;

import com.adminplus.common.properties.LogStorageProperties;
import com.adminplus.service.impl.DatabaseLogStorage;
import com.adminplus.service.impl.LogStorageStrategySelector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * LogStorageStrategySelector 测试类
 *
 * @author AdminPlus
 * @since 2026-04-03
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LogStorageStrategySelector Unit Tests")
class LogStorageStrategySelectorTest {

    @Mock
    private DatabaseLogStorage databaseStorage;

    @Mock
    private LogStorageStrategy elasticsearchStorage;

    @Mock
    private LogStorageProperties logStorageProperties;

    @InjectMocks
    private LogStorageStrategySelector selector;

    @BeforeEach
    void setUp() {
        // Setup strategy names
        lenient().when(databaseStorage.getStrategyName()).thenReturn("DATABASE");
        lenient().when(elasticsearchStorage.getStrategyName()).thenReturn("ELASTICSEARCH");
    }

    @Nested
    @DisplayName("getStrategy Tests")
    class GetStrategyTests {

        @Test
        @DisplayName("should return DATABASE when mode is DATABASE")
        void shouldReturnDatabase_WhenModeIsDatabase() {
            // Given
            when(logStorageProperties.getMode()).thenReturn(LogStorageProperties.StorageMode.DATABASE);

            // Use reflection to set strategies since @RequiredArgsConstructor runs before @Mock
            setStrategies(List.of(databaseStorage, elasticsearchStorage));

            // When
            LogStorageStrategy result = selector.getStrategy();

            // Then
            assertThat(result).isEqualTo(databaseStorage);
        }

        @Test
        @DisplayName("should return ELASTICSEARCH when mode is ELASTICSEARCH and available")
        void shouldReturnElasticsearch_WhenModeIsElasticsearchAndAvailable() {
            // Given
            when(logStorageProperties.getMode()).thenReturn(LogStorageProperties.StorageMode.ELASTICSEARCH);
            when(elasticsearchStorage.isAvailable()).thenReturn(true);
            setStrategies(List.of(databaseStorage, elasticsearchStorage));

            // When
            LogStorageStrategy result = selector.getStrategy();

            // Then
            assertThat(result).isEqualTo(elasticsearchStorage);
        }

        @Test
        @DisplayName("should fallback to DATABASE when mode is ELASTICSEARCH but not available")
        void shouldFallbackToDatabase_WhenModeIsElasticsearchButNotAvailable() {
            // Given
            when(logStorageProperties.getMode()).thenReturn(LogStorageProperties.StorageMode.ELASTICSEARCH);
            when(elasticsearchStorage.isAvailable()).thenReturn(false);
            setStrategies(List.of(databaseStorage, elasticsearchStorage));

            // When
            LogStorageStrategy result = selector.getStrategy();

            // Then
            assertThat(result).isEqualTo(databaseStorage);
        }

        @Test
        @DisplayName("should return ELASTICSEARCH when mode is AUTO and ES available")
        void shouldReturnElasticsearch_WhenModeIsAutoAndAvailable() {
            // Given
            when(logStorageProperties.getMode()).thenReturn(LogStorageProperties.StorageMode.AUTO);
            when(elasticsearchStorage.isAvailable()).thenReturn(true);
            setStrategies(List.of(databaseStorage, elasticsearchStorage));

            // When
            LogStorageStrategy result = selector.getStrategy();

            // Then
            assertThat(result).isEqualTo(elasticsearchStorage);
        }

        @Test
        @DisplayName("should return DATABASE when mode is AUTO and ES not available")
        void shouldReturnDatabase_WhenModeIsAutoAndEsNotAvailable() {
            // Given
            when(logStorageProperties.getMode()).thenReturn(LogStorageProperties.StorageMode.AUTO);
            when(elasticsearchStorage.isAvailable()).thenReturn(false);
            setStrategies(List.of(databaseStorage, elasticsearchStorage));

            // When
            LogStorageStrategy result = selector.getStrategy();

            // Then
            assertThat(result).isEqualTo(databaseStorage);
        }

        @Test
        @DisplayName("should throw exception when strategy not found")
        void shouldThrowException_WhenStrategyNotFound() {
            // Given
            when(logStorageProperties.getMode()).thenReturn(LogStorageProperties.StorageMode.DATABASE);
            setStrategies(List.of(elasticsearchStorage)); // Missing DATABASE strategy

            // When & Then
            assertThatThrownBy(() -> selector.getStrategy())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("未找到存储策略");
        }
    }

    @Nested
    @DisplayName("reset Tests")
    class ResetTests {

        @Test
        @DisplayName("should reset strategy selection")
        void shouldResetStrategySelection() {
            // Given
            when(logStorageProperties.getMode()).thenReturn(LogStorageProperties.StorageMode.DATABASE);
            setStrategies(List.of(databaseStorage));

            // Get strategy first time
            LogStorageStrategy result1 = selector.getStrategy();
            assertThat(result1).isEqualTo(databaseStorage);

            // When
            selector.reset();

            // Then - next call should re-select
            LogStorageStrategy result2 = selector.getStrategy();
            assertThat(result2).isEqualTo(databaseStorage);
        }
    }

    @Nested
    @DisplayName("Caching Tests")
    class CachingTests {

        @Test
        @DisplayName("should cache strategy selection")
        void shouldCacheStrategySelection() {
            // Given
            when(logStorageProperties.getMode()).thenReturn(LogStorageProperties.StorageMode.DATABASE);
            setStrategies(List.of(databaseStorage));

            // When
            LogStorageStrategy result1 = selector.getStrategy();
            LogStorageStrategy result2 = selector.getStrategy();

            // Then
            assertThat(result1).isEqualTo(databaseStorage);
            assertThat(result2).isEqualTo(databaseStorage);
            // Mode should only be queried once due to caching
            verify(logStorageProperties, times(1)).getMode();
        }
    }

    /**
     * Helper method to set strategies via reflection
     */
    private void setStrategies(List<LogStorageStrategy> strategies) {
        try {
            java.lang.reflect.Field field = LogStorageStrategySelector.class.getDeclaredField("strategies");
            field.setAccessible(true);
            field.set(selector, strategies);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}