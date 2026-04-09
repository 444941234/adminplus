package com.adminplus.pojo.dto.request;

import java.util.Optional;

/**
 * 更新菜单请求 DTO
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
public record MenuUpdateRequest(
        Optional<String> parentId,
        Optional<Integer> type,
        Optional<String> name,
        Optional<String> path,
        Optional<String> component,
        Optional<String> permKey,
        Optional<String> icon,
        Optional<Integer> sortOrder,
        Optional<Integer> visible,
        Optional<Integer> status
) {
}