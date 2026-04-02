package com.adminplus.pojo.dto.workflow.hook;

import java.util.List;

/**
 * 钩子执行汇总
 * <p>
 * 包含所有钩子的执行结果和阻断信息
 * </p>
 *
 * @param allPassed          校验类钩子是否全部通过
 * @param validationResults  校验类钩子结果
 * @param executionResults   执行类钩子结果
 * @param blockingMessages   阻断性失败消息
 * @param warningMessages    非阻断性失败消息
 * @author AdminPlus
 * @since 2026-04-02
 */
public record HookExecutionSummary(
    boolean allPassed,
    List<HookResult> validationResults,
    List<HookResult> executionResults,
    List<String> blockingMessages,
    List<String> warningMessages
) {
}
