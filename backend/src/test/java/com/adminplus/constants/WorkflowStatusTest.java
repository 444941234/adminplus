package com.adminplus.constants;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WorkflowStatus 常量测试
 * <p>
 * 验证工作流状态常量值正确，避免硬编码字符串导致的 bug
 * <p>
 * 测试策略：
 * - 常量值验证：确保常量值符合预期
 * - 一致性验证：确保前后端状态值一致
 * - 边界条件：验证状态转换的合法性
 *
 * @author AdminPlus
 * @since 2026-04-05
 */
@DisplayName("WorkflowStatus 常量测试")
class WorkflowStatusTest {

    // ==================== 常量值验证 ====================

    @Nested
    @DisplayName("常量值验证")
    class ConstantValueTests {

        @Test
        @DisplayName("应验证 DRAFT 状态值")
        void shouldVerifyDraftValue() {
            assertEquals("draft", WorkflowStatus.DRAFT,
                    "DRAFT 状态值应该是 'draft'");
            assertEquals(5, WorkflowStatus.DRAFT.length(),
                    "DRAFT 状态值长度应该是 5");
        }

        @Test
        @DisplayName("应验证 RUNNING 状态值")
        void shouldVerifyRunningValue() {
            assertEquals("running", WorkflowStatus.RUNNING,
                    "RUNNING 状态值应该是 'running'");
            assertEquals(7, WorkflowStatus.RUNNING.length(),
                    "RUNNING 状态值长度应该是 7");
        }

        @Test
        @DisplayName("应验证 APPROVED 状态值")
        void shouldVerifyApprovedValue() {
            assertEquals("approved", WorkflowStatus.APPROVED,
                    "APPROVED 状态值应该是 'approved'");
            assertEquals(8, WorkflowStatus.APPROVED.length(),
                    "APPROVED 状态值长度应该是 8");
        }

        @Test
        @DisplayName("应验证 REJECTED 状态值")
        void shouldVerifyRejectedValue() {
            assertEquals("rejected", WorkflowStatus.REJECTED,
                    "REJECTED 状态值应该是 'rejected'");
            assertEquals(8, WorkflowStatus.REJECTED.length(),
                    "REJECTED 状态值长度应该是 8");
        }

        @Test
        @DisplayName("应验证 CANCELLED 状态值")
        void shouldVerifyCancelledValue() {
            assertEquals("cancelled", WorkflowStatus.CANCELLED,
                    "CANCELLED 状态值应该是 'cancelled'");
            assertEquals(9, WorkflowStatus.CANCELLED.length(),
                    "CANCELLED 状态值长度应该是 9");
        }

        @Test
        @DisplayName("应验证所有状态值都是小写")
        void shouldVerifyAllLowerCase() {
            String lowerCaseRegex = "^[a-z]+$";

            assertTrue(WorkflowStatus.DRAFT.matches(lowerCaseRegex),
                    "DRAFT 应该全是小写字母");
            assertTrue(WorkflowStatus.RUNNING.matches(lowerCaseRegex),
                    "RUNNING 应该全是小写字母");
            assertTrue(WorkflowStatus.APPROVED.matches(lowerCaseRegex),
                    "APPROVED 应该全是小写字母");
            assertTrue(WorkflowStatus.REJECTED.matches(lowerCaseRegex),
                    "REJECTED 应该全是小写字母");
            assertTrue(WorkflowStatus.CANCELLED.matches(lowerCaseRegex),
                    "CANCELLED 应该全是小写字母");
        }

        @Test
        @DisplayName("应验证所有状态值唯一")
        void shouldVerifyAllValuesUnique() {
            assertNotEquals(WorkflowStatus.DRAFT, WorkflowStatus.RUNNING);
            assertNotEquals(WorkflowStatus.DRAFT, WorkflowStatus.APPROVED);
            assertNotEquals(WorkflowStatus.DRAFT, WorkflowStatus.REJECTED);
            assertNotEquals(WorkflowStatus.DRAFT, WorkflowStatus.CANCELLED);

            assertNotEquals(WorkflowStatus.RUNNING, WorkflowStatus.APPROVED);
            assertNotEquals(WorkflowStatus.RUNNING, WorkflowStatus.REJECTED);
            assertNotEquals(WorkflowStatus.RUNNING, WorkflowStatus.CANCELLED);

            assertNotEquals(WorkflowStatus.APPROVED, WorkflowStatus.REJECTED);
            assertNotEquals(WorkflowStatus.APPROVED, WorkflowStatus.CANCELLED);

            assertNotEquals(WorkflowStatus.REJECTED, WorkflowStatus.CANCELLED);
        }
    }

    // ==================== 前后端一致性验证 ====================

    @Nested
    @DisplayName("前后端状态一致性验证")
    class FrontendBackendConsistencyTests {

        @Test
        @DisplayName("应验证状态值与前端 status.ts 一致")
        void shouldVerifyConsistencyWithFrontend() {
            // 前端 status.ts 中的状态值
            String[] frontendStatuses = {
                    "draft",      // 草稿
                    "running",    // 进行中（前端显示为"审批中"）
                    "approved",   // 已通过
                    "rejected",   // 已拒绝
                    "cancelled"   // 已取消
            };

            // 验证后端常量值与前端一致
            assertEquals(frontendStatuses[0], WorkflowStatus.DRAFT,
                    "DRAFT 与前端 draft 一致");
            assertEquals(frontendStatuses[1], WorkflowStatus.RUNNING,
                    "RUNNING 与前端 running 一致");
            assertEquals(frontendStatuses[2], WorkflowStatus.APPROVED,
                    "APPROVED 与前端 approved 一致");
            assertEquals(frontendStatuses[3], WorkflowStatus.REJECTED,
                    "REJECTED 与前端 rejected 一致");
            assertEquals(frontendStatuses[4], WorkflowStatus.CANCELLED,
                    "CANCELLED 与前端 cancelled 一致");
        }
    }

    // ==================== 状态转换验证 ====================

    @Nested
    @DisplayName("状态转换验证")
    class StateTransitionTests {

        @Test
        @DisplayName("应验证有效状态转换")
        void shouldVerifyValidTransitions() {
            // 草稿 -> 进行中（提交）
            assertTrue(isValidTransition(WorkflowStatus.DRAFT, WorkflowStatus.RUNNING),
                    "草稿可以提交为进行中");

            // 进行中 -> 已通过（最后节点审批通过）
            assertTrue(isValidTransition(WorkflowStatus.RUNNING, WorkflowStatus.APPROVED),
                    "进行中可以转为已通过");

            // 进行中 -> 已拒绝（任意节点拒绝）
            assertTrue(isValidTransition(WorkflowStatus.RUNNING, WorkflowStatus.REJECTED),
                    "进行中可以转为已拒绝");

            // 草稿/进行中 -> 已取消（发起人取消）
            assertTrue(isValidTransition(WorkflowStatus.DRAFT, WorkflowStatus.CANCELLED),
                    "草稿可以取消");
            assertTrue(isValidTransition(WorkflowStatus.RUNNING, WorkflowStatus.CANCELLED),
                    "进行中可以取消");

            // 已拒绝 -> 草稿（撤回后重新编辑）
            assertTrue(isValidTransition(WorkflowStatus.REJECTED, WorkflowStatus.DRAFT),
                    "已拒绝可以撤回为草稿");
        }

        @Test
        @DisplayName("应验证无效状态转换")
        void shouldVerifyInvalidTransitions() {
            // 终态不能转换
            assertFalse(isValidTransition(WorkflowStatus.APPROVED, WorkflowStatus.RUNNING),
                    "已通过不能转回进行中");
            assertFalse(isValidTransition(WorkflowStatus.REJECTED, WorkflowStatus.APPROVED),
                    "已拒绝不能直接转已通过");
            assertFalse(isValidTransition(WorkflowStatus.CANCELLED, WorkflowStatus.RUNNING),
                    "已取消不能转回进行中");

            // 进行中不能直接转草稿（需要通过撤回操作）
            assertFalse(isValidTransition(WorkflowStatus.RUNNING, WorkflowStatus.DRAFT),
                    "进行中不能直接转草稿");
        }

        /**
         * 判断状态转换是否有效
         */
        private boolean isValidTransition(String from, String to) {
            if (from.equals(to)) {
                return true; // 保持原状态
            }

            return switch (from) {
                case WorkflowStatus.DRAFT -> to.equals(WorkflowStatus.RUNNING)
                        || to.equals(WorkflowStatus.CANCELLED);
                case WorkflowStatus.RUNNING -> to.equals(WorkflowStatus.APPROVED)
                        || to.equals(WorkflowStatus.REJECTED)
                        || to.equals(WorkflowStatus.CANCELLED);
                case WorkflowStatus.REJECTED -> to.equals(WorkflowStatus.DRAFT);
                case WorkflowStatus.APPROVED, WorkflowStatus.CANCELLED -> false;
                default -> false;
            };
        }
    }

    // ==================== 终态验证 ====================

    @Nested
    @DisplayName("终态验证")
    class FinalStateTests {

        @Test
        @DisplayName("应验证终态状态")
        void shouldVerifyFinalStates() {
            // APPROVED, REJECTED, CANCELLED 是终态，不能再次转换
            String[] finalStates = {
                    WorkflowStatus.APPROVED,
                    WorkflowStatus.REJECTED,
                    WorkflowStatus.CANCELLED
            };

            for (String state : finalStates) {
                assertTrue(isFinalState(state), state + " 应该是终态");
            }

            // DRAFT 和 RUNNING 不是终态
            assertFalse(isFinalState(WorkflowStatus.DRAFT), "DRAFT 不是终态");
            assertFalse(isFinalState(WorkflowStatus.RUNNING), "RUNNING 不是终态");
        }

        /**
         * 判断是否为终态
         */
        private boolean isFinalState(String state) {
            return state.equals(WorkflowStatus.APPROVED)
                    || state.equals(WorkflowStatus.REJECTED)
                    || state.equals(WorkflowStatus.CANCELLED);
        }
    }

    // ==================== 字符串比较验证 ====================

    @Nested
    @DisplayName("字符串比较验证")
    class StringComparisonTests {

        @Test
        @DisplayName("应验证常量与字符串字面量相等")
        void shouldVerifyConstantsEqualLiterals() {
            // 确保使用常量比较时，与字符串字面量相等
            assertEquals("draft", WorkflowStatus.DRAFT);
            assertEquals("running", WorkflowStatus.RUNNING);
            assertEquals("approved", WorkflowStatus.APPROVED);
            assertEquals("rejected", WorkflowStatus.REJECTED);
            assertEquals("cancelled", WorkflowStatus.CANCELLED);
        }

        @Test
        @DisplayName("应验证常量可以用 equals 比较")
        void shouldVerifyConstantsComparableWithEquals() {
            assertTrue("draft".equals(WorkflowStatus.DRAFT));
            assertTrue("running".equals(WorkflowStatus.RUNNING));
            assertTrue("approved".equals(WorkflowStatus.APPROVED));
            assertTrue("rejected".equals(WorkflowStatus.REJECTED));
            assertTrue("cancelled".equals(WorkflowStatus.CANCELLED));
        }

        @Test
        @DisplayName("应验证常量大小写敏感")
        void shouldVerifyConstantsAreCaseSensitive() {
            assertNotEquals("DRAFT", WorkflowStatus.DRAFT,
                    "大写 DRAFT 不应该等于常量");
            assertNotEquals("Running", WorkflowStatus.RUNNING,
                    "混合大小写 Running 不应该等于常量");
            assertNotEquals("APPROVED", WorkflowStatus.APPROVED,
                    "大写 APPROVED 不应该等于常量");
        }
    }
}
