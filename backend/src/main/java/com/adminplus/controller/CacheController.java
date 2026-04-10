package com.adminplus.controller;
import com.adminplus.enums.OperationType;

import com.adminplus.common.annotation.OperationLog;
import com.adminplus.common.pojo.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * 缓存管理控制器
 *
 * Spring Cache 的 Redis key 格式为: cacheName::key
 * 例如: roles::all, dashboardStats::stats
 *
 * @author AdminPlus
 * @since 2026-04-03
 */
@Slf4j
@RestController
@RequestMapping(value = "/sys/cache")
@RequiredArgsConstructor
@Tag(name = "缓存管理", description = "Redis缓存清除和管理")
public class CacheController {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 紧急清理端点 - 无需认证
     * 用于解决缓存序列化格式变更导致的反序列化异常
     */
    @DeleteMapping("/emergency-clear")
    @Operation(summary = "紧急清理所有缓存（无需认证）")
    public ApiResponse<Void> emergencyClearCache() {
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("紧急清理所有缓存: {} 个键", keys.size());
        }
        return ApiResponse.ok();
    }

    @DeleteMapping("/clear-all")
    @Operation(summary = "清除所有缓存")
    @OperationLog(module = "缓存管理", type = OperationType.DELETE, description = "清除所有缓存")
    @PreAuthorize("hasAuthority('cache:clear')")
    public ApiResponse<Void> clearAllCache() {
        // Spring Cache key 格式: cacheName::key
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("清除所有缓存: {} 个键", keys.size());
        }
        return ApiResponse.ok();
    }

    @DeleteMapping("/clear/{cacheName}")
    @Operation(summary = "清除指定缓存")
    @OperationLog(module = "缓存管理", type = OperationType.DELETE, description = "清除缓存 {#cacheName}")
    @PreAuthorize("hasAuthority('cache:clear')")
    public ApiResponse<Void> clearCache(@PathVariable String cacheName) {
        // Spring Cache key 格式: cacheName::*
        Set<String> keys = redisTemplate.keys(cacheName + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("清除缓存 {}: {} 个键", cacheName, keys.size());
        }
        return ApiResponse.ok();
    }

    @GetMapping("/keys")
    @Operation(summary = "获取所有缓存键")
    @OperationLog(module = "缓存管理", type = OperationType.QUERY, description = "查询缓存键列表")
    @PreAuthorize("hasAuthority('cache:list')")
    public ApiResponse<Set<String>> getCacheKeys() {
        Set<String> keys = redisTemplate.keys("*");
        return ApiResponse.ok(keys);
    }

    @DeleteMapping("/clear-roles")
    @Operation(summary = "清除角色缓存")
    @OperationLog(module = "缓存管理", type = OperationType.DELETE, description = "清除角色缓存")
    @PreAuthorize("hasAuthority('cache:clear')")
    public ApiResponse<Void> clearRolesCache() {
        Set<String> keys = redisTemplate.keys("roles*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("清除角色缓存: {} 个键", keys.size());
        }
        return ApiResponse.ok();
    }
}