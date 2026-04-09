package com.adminplus.pojo.dto.response;

import java.util.List;

/**
 * 配置生效信息视图对象
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public record ConfigEffectInfoResp(
        List<PendingEffect> pendingEffects,
        List<String> restartRequiredConfigs
) {

    /**
     * 待生效配置信息
     */
    public record PendingEffect(
            String key,
            String name,
            String newValue,
            String effectType,
            String updateTime
    ) {}
}
