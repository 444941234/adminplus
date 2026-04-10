package com.adminplus.utils.masking;

/**
 * 日志脱敏器接口
 * <p>
 * 实现此接口并注册为 SPI 服务即可自动加入脱敏链
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
@FunctionalInterface
public interface LogMasker {
    /**
     * 对输入进行脱敏处理
     *
     * @param input 原始输入
     * @return 脱敏后的输出
     */
    String mask(String input);
}
