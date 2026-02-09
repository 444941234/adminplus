package com.adminplus.dto;

import lombok.Builder;

import java.util.Optional;

/**
 * 部门更新请求
 *
 * @author AdminPlus
 * @since 2026-02-09
 */
@Builder
public record DeptUpdateReq(
        Optional<String> parentId,
        Optional<String> name,
        Optional<String> code,
        Optional<String> leader,
        Optional<String> phone,
        Optional<String> email,
        Optional<Integer> sortOrder,
        Optional<Integer> status
) {
}