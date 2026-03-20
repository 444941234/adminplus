package com.adminplus.pojo.dto.resp;

import java.util.List;

/**
 * 用户活动统计视图对象
 *
 * @author AdminPlus
 * @since 2026-03-20
 */
public record ActivityStatsResp(
        Integer daysActive,
        Integer totalActions,
        String lastLogin,
        String lastLoginIp,
        List<ActivityItemResp> recentActivity
) {
}
