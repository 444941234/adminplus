package com.adminplus.utils;

import com.adminplus.common.exception.BizException;
import com.adminplus.service.DeptService;

import java.util.List;

/**
 * 权限检查助手工具类
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public final class PermissionHelper {

    private PermissionHelper() {}

    /**
     * 检查当前用户是否为资源所有者或管理员
     */
    public static void checkOwnerOrAdmin(String resourceOwnerId, String operation) {
        if (!SecurityUtils.isAdmin()
            && !resourceOwnerId.equals(SecurityUtils.getCurrentUserId())) {
            throw new BizException(403, "无权执行该操作: " + operation);
        }
    }

    public static void checkOwnerOrAdminForView(String resourceOwnerId) {
        checkOwnerOrAdmin(resourceOwnerId, "查看");
    }

    public static void checkOwnerOrAdminForEdit(String resourceOwnerId) {
        checkOwnerOrAdmin(resourceOwnerId, "修改");
    }

    public static void checkOwnerOrAdminForDelete(String resourceOwnerId) {
        checkOwnerOrAdmin(resourceOwnerId, "删除");
    }

    /**
     * 获取当前用户可访问的部门ID列表
     */
    public static List<String> getAccessibleDeptIds(DeptService deptService) {
        if (SecurityUtils.isAdmin()) {
            return List.of();
        }
        String currentDeptId = SecurityUtils.getCurrentUserDeptId();
        if (currentDeptId == null) {
            return List.of();
        }
        return deptService.getDeptAndChildrenIds(currentDeptId);
    }

    /**
     * 判断是否需要按部门过滤
     */
    public static boolean needDeptFilter() {
        return !SecurityUtils.isAdmin()
            && SecurityUtils.getCurrentUserDeptId() != null;
    }
}