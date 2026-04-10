package com.adminplus.controller;

import com.adminplus.service.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CacheController 测试类
 *
 * @author AdminPlus
 * @since 2026-04-04
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CacheController Unit Tests")
class CacheControllerTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private CacheController cacheController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cacheController).build();
    }

    @Nested
    @DisplayName("Clear All Cache Tests")
    class ClearAllCacheTests {

        @Test
        @DisplayName("should clear all cache when user has permission")
        @WithMockUser(authorities = "cache:clear")
        void clearAllCache_ShouldSucceed() throws Exception {
            // Given
            Set<String> keys = Set.of("roles::all", "userPermissions::user-001");
            when(redisTemplate.keys("*")).thenReturn(keys);
            when(redisTemplate.delete((java.util.Collection<String>) keys)).thenReturn(2L);

            // When & Then
            mockMvc.perform(delete("/sys/cache/clear-all")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(redisTemplate).keys("*");
            verify(redisTemplate).delete(keys);
        }

        @Test
        @DisplayName("should handle empty cache")
        @WithMockUser(authorities = "cache:clear")
        void clearAllCache_WhenEmpty_ShouldSucceed() throws Exception {
            // Given
            when(redisTemplate.keys("*")).thenReturn(null);

            // When & Then
            mockMvc.perform(delete("/sys/cache/clear-all")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(redisTemplate).keys("*");
            verify(redisTemplate, never()).delete(any(java.util.Collection.class));
        }
    }

    @Nested
    @DisplayName("Clear Specific Cache Tests")
    class ClearCacheTests {

        @Test
        @DisplayName("should clear specific cache by name")
        @WithMockUser(authorities = "cache:clear")
        void clearCache_ShouldSucceed() throws Exception {
            // Given
            Set<String> keys = Set.of("roles::all", "roles::nonAdmin");
            when(redisTemplate.keys("roles*")).thenReturn(keys);
            when(redisTemplate.delete((java.util.Collection<String>) keys)).thenReturn(2L);

            // When & Then
            mockMvc.perform(delete("/sys/cache/clear/roles")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(redisTemplate).keys("roles*");
            verify(redisTemplate).delete(keys);
        }
    }

    @Nested
    @DisplayName("Clear Roles Cache Tests")
    class ClearRolesCacheTests {

        @Test
        @DisplayName("should clear roles cache")
        @WithMockUser(authorities = "cache:clear")
        void clearRolesCache_ShouldSucceed() throws Exception {
            // Given
            Set<String> keys = Set.of("roles::all");
            when(redisTemplate.keys("roles*")).thenReturn(keys);
            when(redisTemplate.delete((java.util.Collection<String>) keys)).thenReturn(1L);

            // When & Then
            mockMvc.perform(delete("/sys/cache/clear-roles")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(redisTemplate).keys("roles*");
            verify(redisTemplate).delete(keys);
        }
    }

    @Nested
    @DisplayName("Get Cache Keys Tests")
    class GetCacheKeysTests {

        @Test
        @DisplayName("should return all cache keys")
        @WithMockUser(authorities = "cache:list")
        void getCacheKeys_ShouldReturnKeys() throws Exception {
            // Given
            Set<String> keys = Set.of("roles::all", "userPermissions::user-001");
            when(redisTemplate.keys("*")).thenReturn(keys);

            // When & Then
            mockMvc.perform(get("/sys/cache/keys")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());

            verify(redisTemplate).keys("*");
        }

        @Test
        @DisplayName("should return empty array when no keys")
        @WithMockUser(authorities = "cache:list")
        void getCacheKeys_WhenEmpty_ShouldReturnEmptyArray() throws Exception {
            // Given
            when(redisTemplate.keys("*")).thenReturn(null);

            // When & Then
            mockMvc.perform(get("/sys/cache/keys")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }
}