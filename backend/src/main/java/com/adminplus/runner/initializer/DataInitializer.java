package com.adminplus.runner.initializer;

/**
 * 数据初始化器接口
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public interface DataInitializer {

    /**
     * 初始化器顺序（数字越小越先执行）
     */
    int getOrder();

    /**
     * 初始化器名称
     */
    String getName();

    /**
     * 执行初始化
     */
    void initialize() throws Exception;
}
