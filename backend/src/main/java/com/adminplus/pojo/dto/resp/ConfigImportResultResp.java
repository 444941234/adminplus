package com.adminplus.pojo.dto.resp;

import java.util.List;

/**
 * 配置导入结果视图对象
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public record ConfigImportResultResp(
        Integer total,
        Integer success,
        Integer skipped,
        Integer failed,
        List<ImportDetail> details
) {

    /**
     * 导入详情信息
     */
    public record ImportDetail(
            String key,
            String status,
            String reason
    ) {}
}
