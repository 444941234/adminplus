package com.adminplus.pojo.dto.req;

import lombok.Builder;

import java.util.Optional;

/**
 * 部门更新请求
 * <p>
 * 使用 Optional 实现 PATCH 语义：仅更新存在的字段
 * 字段段格式校验在 Service 层处理（Bean Validation 不支持 Optional 类型）
 * </p>
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
