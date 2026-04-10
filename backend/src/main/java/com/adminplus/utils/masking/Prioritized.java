package com.adminplus.utils.masking;

/**
 * 优先级接口
 * <p>
 * 脱敏器可实现此接口指定执行顺序，数字越小优先级越高
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public interface Prioritized {
    /**
     * 获取优先级（默认 1000）
     * <p>
     * 内置规则优先级范围：100-700
     * <ul>
     *   <li>密码: 100</li>
     *   <li>Token: 200</li>
     *   <li>身份证: 300</li>
     *   <li>手机号: 400</li>
     *   <li>邮箱: 500</li>
     *   <li>SQL密码: 600</li>
     *   <li>信用卡: 700</li>
     * </ul>
     */
    default int getPriority() {
        return 1000;
    }
}
