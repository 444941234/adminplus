package com.adminplus.service.workflow.hook;

import com.adminplus.pojo.dto.workflow.hook.HookContext;
import com.adminplus.pojo.dto.workflow.hook.HookExecutorConfig;
import com.adminplus.pojo.dto.workflow.hook.HookResult;

/**
 * 钩子执行器接口
 * <p>
 * 所有钩子执行器必须实现此接口
 * </p>
 *
 * @author AdminPlus
 * @since 2026-04-02
 */
public interface HookExecutor {

    /**
     * 执行钩子
     *
     * @param config  执行器配置
     * @param context 执行上下文
     * @return 执行结果
     */
    HookResult execute(HookExecutorConfig config, HookContext context);

    /**
     * 获取执行器类型
     *
     * @return 执行器类型（spel、bean、http）
     */
    String getType();
}
