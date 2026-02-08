package com.adminplus.vo;

import java.util.List;

/**
 * 图表数据视图对象
 *
 * @author AdminPlus
 * @since 2026-02-08
 */
public record ChartDataVO(
        /**
         * 数据标签（x轴）
         */
        List<String> labels,

        /**
         * 数据值（y轴）
         */
        List<Long> values
) {
}