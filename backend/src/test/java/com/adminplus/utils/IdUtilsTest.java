package com.adminplus.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * IdUtils 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
class IdUtilsTest {

    @Test
    void nextId_ShouldGenerateUniqueIds() {
        // Given
        long id1 = IdUtils.nextId();
        long id2 = IdUtils.nextId();

        // Then
        assertThat(id1).isNotEqualTo(id2);
        assertThat(id1).isPositive();
        assertThat(id2).isPositive();
    }

    @Test
    void nextIdStr_ShouldReturnStringId() {
        // When
        String id = IdUtils.nextIdStr();

        // Then
        assertThat(id).isNotEmpty();
        assertThat(id).isInstanceOf(String.class);
    }

    @Test
    void nextId_ShouldGenerateIncreasingIds() {
        // Given
        long id1 = IdUtils.nextId();
        long id2 = IdUtils.nextId();
        long id3 = IdUtils.nextId();

        // Then
        assertThat(id2).isGreaterThan(id1);
        assertThat(id3).isGreaterThan(id2);
    }

    @Test
    void nextId_ShouldGenerateIdsWithinValidRange() {
        // When
        long id = IdUtils.nextId();

        // Then - Snowflake IDs should be within reasonable range
        assertThat(id).isLessThan(Long.MAX_VALUE);
        assertThat(id).isGreaterThan(0);
    }

    @Test
    void nextId_CalledMultipleTimes_ShouldGenerateUniqueIds() {
        // Given
        int count = 1000;
        java.util.Set<Long> ids = new java.util.HashSet<>();

        // When
        for (int i = 0; i < count; i++) {
            ids.add(IdUtils.nextId());
        }

        // Then
        assertThat(ids).hasSize(count);
    }
}
