package com.adminplus.runner.initializer;

import com.adminplus.pojo.entity.WorkflowDefinitionEntity;
import com.adminplus.pojo.entity.WorkflowNodeEntity;
import com.adminplus.repository.WorkflowDefinitionRepository;
import com.adminplus.repository.WorkflowNodeRepository;
import com.adminplus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 工作流定义数据初始化器
 * <p>
 * 初始化常用的工作流模板，如请假申请、报销申请等
 * </p>
 *
 * @author AdminPlus
 * @since 2026-04-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowDefinitionInitializer implements DataInitializer {

    private final WorkflowDefinitionRepository definitionRepository;
    private final WorkflowNodeRepository nodeRepository;
    private final UserRepository userRepository;

    @Override
    public int getOrder() {
        return 7;
    }

    @Override
    public String getName() {
        return "工作流定义数据初始化";
    }

    @Override
    @Transactional
    public void initialize() {
        // 检查是否已有工作流定义节点数据（更完整的检查）
        long definitionCount = definitionRepository.count();
        long nodeCount = nodeRepository.count();

        if (definitionCount > 0 && nodeCount > 0) {
            log.info("工作流定义数据已存在（{} 个定义，{} 个节点），跳过初始化",
                    definitionCount, nodeCount);
            return;
        }

        // 如果有定义但没节点，或者两者都没有，都进行初始化
        log.info("开始初始化工作流定义数据（当前: {} 个定义，{} 个节点）",
                definitionCount, nodeCount);

        // 获取 admin 用户 ID
        String adminUserId = userRepository.findByUsername("admin")
                .map(u -> u.getId())
                .orElse("system");

        List<WorkflowDefinitionEntity> definitions = new ArrayList<>();

        // 1. 请假申请流程（简单流程）
        definitions.add(createLeaveRequestWorkflow(adminUserId));

        // 2. 报销申请流程（条件分支）
        definitions.add(createExpenseClaimWorkflow(adminUserId));

        // 3. 采购申请流程（多级审批）
        definitions.add(createPurchaseRequestWorkflow(adminUserId));

        // 4. 合同审批流程（复杂流程 - 条件分支 + 会签 + 并行审批）
        definitions.add(createContractApprovalWorkflow(adminUserId));

        // 保存工作流定义
        List<WorkflowDefinitionEntity> savedDefinitions = definitionRepository.saveAll(definitions);

        // 创建工作流节点
        List<WorkflowNodeEntity> nodes = new ArrayList<>();
        for (WorkflowDefinitionEntity definition : savedDefinitions) {
            if ("leave_request".equals(definition.getDefinitionKey())) {
                nodes.addAll(createLeaveRequestNodes(definition, adminUserId));
            } else if ("expense_claim".equals(definition.getDefinitionKey())) {
                nodes.addAll(createExpenseClaimNodes(definition, adminUserId));
            } else if ("purchase_request".equals(definition.getDefinitionKey())) {
                nodes.addAll(createPurchaseRequestNodes(definition, adminUserId));
            } else if ("contract_approval".equals(definition.getDefinitionKey())) {
                nodes.addAll(createContractApprovalNodes(definition, adminUserId));
            }
        }

        nodeRepository.saveAll(nodes);

        log.info("初始化工作流定义数据完成，共 {} 个定义，{} 个节点",
                savedDefinitions.size(), nodes.size());
    }

    /**
     * 创建请假申请工作流定义
     */
    private WorkflowDefinitionEntity createLeaveRequestWorkflow(String userId) {
        WorkflowDefinitionEntity definition = new WorkflowDefinitionEntity();
        definition.setDefinitionName("请假申请");
        definition.setDefinitionKey("leave_request");
        definition.setCategory("人力资源");
        definition.setDescription("员工请假申请审批流程，包括事假、病假、年假等");
        definition.setStatus(1);
        definition.setVersion(1);
        definition.setFormConfig(buildLeaveRequestFormConfig());
        definition.setCreateUser(userId);
        definition.setUpdateUser(userId);
        return definition;
    }

    /**
     * 创建请假申请节点
     */
    private List<WorkflowNodeEntity> createLeaveRequestNodes(WorkflowDefinitionEntity definition, String userId) {
        List<WorkflowNodeEntity> nodes = new ArrayList<>();

        // 部门经理审批
        nodes.add(createNode(definition, "部门审批", "dept_approval",
                1, "role", "ROLE_MANAGER", false, true, "部门经理审批", userId));

        // 人事审批（请假天数>=3天）
        nodes.add(createNode(definition, "人事审批", "hr_approval",
                2, "role", "ROLE_HR", false, true, "人事部门审批", userId));

        return nodes;
    }

    /**
     * 创建报销申请工作流定义
     */
    private WorkflowDefinitionEntity createExpenseClaimWorkflow(String userId) {
        WorkflowDefinitionEntity definition = new WorkflowDefinitionEntity();
        definition.setDefinitionName("报销申请");
        definition.setDefinitionKey("expense_claim");
        definition.setCategory("财务");
        definition.setDescription("费用报销申请审批流程");
        definition.setStatus(1);
        definition.setVersion(1);
        definition.setFormConfig(buildExpenseClaimFormConfig());
        definition.setCreateUser(userId);
        definition.setUpdateUser(userId);
        return definition;
    }

    /**
     * 创建报销申请节点
     */
    private List<WorkflowNodeEntity> createExpenseClaimNodes(WorkflowDefinitionEntity definition, String userId) {
        List<WorkflowNodeEntity> nodes = new ArrayList<>();

        // 部门经理审批
        nodes.add(createNode(definition, "部门审批", "dept_approval",
                1, "role", "ROLE_MANAGER", false, true, "部门经理审批", userId));

        // 财务审批（金额>=5000）
        nodes.add(createNode(definition, "财务审批", "finance_approval",
                2, "role", "ROLE_FINANCE", false, true, "财务部门审批", userId));

        return nodes;
    }

    /**
     * 创建采购申请工作流定义
     */
    private WorkflowDefinitionEntity createPurchaseRequestWorkflow(String userId) {
        WorkflowDefinitionEntity definition = new WorkflowDefinitionEntity();
        definition.setDefinitionName("采购申请");
        definition.setDefinitionKey("purchase_request");
        definition.setCategory("采购");
        definition.setDescription("物品采购申请审批流程");
        definition.setStatus(1);
        definition.setVersion(1);
        definition.setFormConfig(buildPurchaseRequestFormConfig());
        definition.setCreateUser(userId);
        definition.setUpdateUser(userId);
        return definition;
    }

    /**
     * 创建采购申请节点
     */
    private List<WorkflowNodeEntity> createPurchaseRequestNodes(WorkflowDefinitionEntity definition, String userId) {
        List<WorkflowNodeEntity> nodes = new ArrayList<>();

        // 部门经理审批
        nodes.add(createNode(definition, "部门审批", "dept_approval",
                1, "role", "ROLE_MANAGER", false, true, "部门经理审批", userId));

        // 采购部审批
        nodes.add(createNode(definition, "采购部审批", "purchase_approval",
                2, "role", "ROLE_PURCHASE", false, true, "采购部门审批", userId));

        // 财务审批（金额>=10000）
        nodes.add(createNode(definition, "财务审批", "finance_approval",
                3, "role", "ROLE_FINANCE", false, true, "财务部门审批", userId));

        return nodes;
    }

    /**
     * 创建工作流节点
     */
    private WorkflowNodeEntity createNode(WorkflowDefinitionEntity definition,
                                          String nodeName, String nodeCode,
                                          int nodeOrder, String approverType,
                                          String approverId, boolean isCounterSign,
                                          boolean autoPassSameUser, String description, String userId) {
        WorkflowNodeEntity node = new WorkflowNodeEntity();
        node.setDefinitionId(definition.getId());
        node.setNodeName(nodeName);
        node.setNodeCode(nodeCode);
        node.setNodeOrder(nodeOrder);
        node.setApproverType(approverType);
        node.setApproverId(approverId);
        node.setIsCounterSign(isCounterSign);
        node.setAutoPassSameUser(autoPassSameUser);
        node.setDescription(description);
        node.setCreateUser(userId);
        node.setUpdateUser(userId);
        return node;
    }

    /**
     * 构建请假申请表单配置
     */
    private String buildLeaveRequestFormConfig() {
        return """
                {
                  "fields": [
                    {
                      "field": "leaveType",
                      "label": "请假类型",
                      "type": "select",
                      "required": true,
                      "options": [
                        {"label": "事假", "value": "personal"},
                        {"label": "病假", "value": "sick"},
                        {"label": "年假", "value": "annual"},
                        {"label": "调休", "value": "comp"}
                      ]
                    },
                    {
                      "field": "startDate",
                      "label": "开始日期",
                      "type": "date",
                      "required": true
                    },
                    {
                      "field": "endDate",
                      "label": "结束日期",
                      "type": "date",
                      "required": true
                    },
                    {
                      "field": "days",
                      "label": "请假天数",
                      "type": "number",
                      "required": true
                    },
                    {
                      "field": "reason",
                      "label": "请假事由",
                      "type": "textarea",
                      "required": true
                    }
                  ]
                }
                """;
    }

    /**
     * 构建报销申请表单配置
     */
    private String buildExpenseClaimFormConfig() {
        return """
                {
                  "fields": [
                    {
                      "field": "expenseType",
                      "label": "报销类型",
                      "type": "select",
                      "required": true,
                      "options": [
                        {"label": "交通费", "value": "transport"},
                        {"label": "住宿费", "value": "accommodation"},
                        {"label": "餐饮费", "value": "meal"},
                        {"label": "办公用品", "value": "office"},
                        {"label": "其他", "value": "other"}
                      ]
                    },
                    {
                      "field": "amount",
                      "label": "报销金额",
                      "type": "number",
                      "required": true
                    },
                    {
                      "field": "expenseDate",
                      "label": "费用发生日期",
                      "type": "date",
                      "required": true
                    },
                    {
                      "field": "description",
                      "label": "费用说明",
                      "type": "textarea",
                      "required": true
                    },
                    {
                      "field": "attachments",
                      "label": "附件",
                      "type": "file",
                      "required": false
                    }
                  ]
                }
                """;
    }

    /**
     * 构建采购申请表单配置
     */
    private String buildPurchaseRequestFormConfig() {
        return """
                {
                  "fields": [
                    {
                      "field": "itemType",
                      "label": "物品类型",
                      "type": "select",
                      "required": true,
                      "options": [
                        {"label": "办公设备", "value": "equipment"},
                        {"label": "办公用品", "value": "supplies"},
                        {"label": "软件服务", "value": "software"},
                        {"label": "其他", "value": "other"}
                      ]
                    },
                    {
                      "field": "itemName",
                      "label": "物品名称",
                      "type": "text",
                      "required": true
                    },
                    {
                      "field": "quantity",
                      "label": "数量",
                      "type": "number",
                      "required": true
                    },
                    {
                      "field": "unitPrice",
                      "label": "单价",
                      "type": "number",
                      "required": true
                    },
                    {
                      "field": "totalAmount",
                      "label": "总金额",
                      "type": "number",
                      "required": true
                    },
                    {
                      "field": "reason",
                      "label": "申请理由",
                      "type": "textarea",
                      "required": true
                    }
                  ]
                }
                """;
    }

    /**
     * 创建合同审批工作流定义（复杂流程示例）
     * <p>
     * 特性：
     * - 条件分支：根据合同金额走不同审批路径
     * - 会签节点：法务和财务需要同时审批
     * - 并行审批：总经理和总监可并行审批
     * - 抄送功能：关键节点抄送给相关人员
     * </p>
     */
    private WorkflowDefinitionEntity createContractApprovalWorkflow(String userId) {
        WorkflowDefinitionEntity definition = new WorkflowDefinitionEntity();
        definition.setDefinitionName("合同审批");
        definition.setDefinitionKey("contract_approval");
        definition.setCategory("法务财务");
        definition.setDescription("企业合同签订审批流程，包含条件分支、会签、并行审批等高级特性");
        definition.setStatus(1);
        definition.setVersion(1);
        definition.setFormConfig(buildContractApprovalFormConfig());
        definition.setCreateUser(userId);
        definition.setUpdateUser(userId);
        return definition;
    }

    /**
     * 创建合同审批节点（复杂流程）
     */
    private List<WorkflowNodeEntity> createContractApprovalNodes(WorkflowDefinitionEntity definition, String userId) {
        List<WorkflowNodeEntity> nodes = new ArrayList<>();

        // 节点1: 部门经理初审（所有合同必经）
        nodes.add(createNode(definition, "部门初审", "dept_review",
                1, "role", "ROLE_MANAGER", false, true,
                "部门经理对合同进行初步审核", userId));

        // 节点2: 法务审核（合同金额>=10万或法务审查需求）
        // 条件: amount >= 100000 || needLegalReview == true
        WorkflowNodeEntity legalReview = createNode(definition, "法务审核", "legal_review",
                2, "role", "ROLE_LEGAL", false, true,
                "法务部门审核合同条款和风险", userId);
        legalReview.setConditionExpression("#amount >= 100000 or #needLegalReview == true");
        nodes.add(legalReview);

        // 节点3: 财务审核（合同金额>=5万）
        // 条件: amount >= 50000
        WorkflowNodeEntity financeReview = createNode(definition, "财务审核", "finance_review",
                3, "role", "ROLE_FINANCE", false, true,
                "财务部门审核合同金额和付款条款", userId);
        financeReview.setConditionExpression("#amount >= 50000");
        nodes.add(financeReview);

        // 节点4: 会签节点（法务+财务同时审批，合同金额>=50万）
        // 条件: amount >= 500000
        WorkflowNodeEntity jointReview = createNode(definition, "法务财务会签", "joint_review",
                4, "role", "ROLE_LEGAL,ROLE_FINANCE", true, false,
                "重大合同需要法务和财务部门同时审批", userId);
        jointReview.setConditionExpression("#amount >= 500000");
        nodes.add(jointReview);

        // 节点5: 业务总监审批（合同金额>=20万且<50万）
        // 条件: amount >= 200000 and amount < 500000
        WorkflowNodeEntity directorApproval = createNode(definition, "总监审批", "director_approval",
                5, "role", "ROLE_DIRECTOR", false, true,
                "业务总监审批", userId);
        directorApproval.setConditionExpression("#amount >= 200000 and #amount < 500000");
        nodes.add(directorApproval);

        // 节点6: 副总经理审批（合同金额>=50万且<100万）
        // 条件: amount >= 500000 and amount < 1000000
        WorkflowNodeEntity vpApproval = createNode(definition, "副总经理审批", "vp_approval",
                6, "role", "ROLE_VICE_PRESIDENT", false, true,
                "副总经理审批", userId);
        vpApproval.setConditionExpression("#amount >= 500000 and #amount < 1000000");
        nodes.add(vpApproval);

        // 节点7: 总经理审批（合同金额>=100万）
        // 条件: amount >= 1000000
        WorkflowNodeEntity gmApproval = createNode(definition, "总经理审批", "gm_approval",
                7, "role", "ROLE_PRESIDENT", false, false,
                "总经理审批（重大合同）", userId);
        gmApproval.setConditionExpression("#amount >= 1000000");
        nodes.add(gmApproval);

        return nodes;
    }

    /**
     * 构建合同审批表单配置（复杂表单）
     */
    private String buildContractApprovalFormConfig() {
        return """
                {
                  "fields": [
                    {
                      "field": "contractType",
                      "label": "合同类型",
                      "type": "select",
                      "required": true,
                      "options": [
                        {"label": "销售合同", "value": "sales"},
                        {"label": "采购合同", "value": "purchase"},
                        {"label": "服务合同", "value": "service"},
                        {"label": "租赁合同", "value": "lease"},
                        {"label": "战略合作协议", "value": "strategic"},
                        {"label": "保密协议", "value": "nda"},
                        {"label": "其他", "value": "other"}
                      ]
                    },
                    {
                      "field": "contractTitle",
                      "label": "合同标题",
                      "type": "text",
                      "required": true,
                      "placeholder": "请输入合同名称"
                    },
                    {
                      "field": "counterparty",
                      "label": "对方单位",
                      "type": "text",
                      "required": true,
                      "placeholder": "请输入合作方全称"
                    },
                    {
                      "field": "contractAmount",
                      "label": "合同金额（元）",
                      "type": "number",
                      "required": true,
                      "min": 0,
                      "placeholder": "请输入合同总金额"
                    },
                    {
                      "field": "currency",
                      "label": "币种",
                      "type": "select",
                      "required": true,
                      "options": [
                        {"label": "人民币", "value": "CNY"},
                        {"label": "美元", "value": "USD"},
                        {"label": "欧元", "value": "EUR"}
                      ],
                      "default": "CNY"
                    },
                    {
                      "field": "startDate",
                      "label": "合同开始日期",
                      "type": "date",
                      "required": true
                    },
                    {
                      "field": "endDate",
                      "label": "合同结束日期",
                      "type": "date",
                      "required": true
                    },
                    {
                      "field": "paymentTerms",
                      "label": "付款方式",
                      "type": "select",
                      "required": true,
                      "options": [
                        {"label": "一次性付款", "value": "onetime"},
                        {"label": "分期付款", "value": "installment"},
                        {"label": "按进度付款", "value": "milestone"},
                        {"label": "账期付款", "value": "credit"}
                      ]
                    },
                    {
                      "field": "needLegalReview",
                      "label": "是否需要法务审查",
                      "type": "checkbox",
                      "default": false
                    },
                    {
                      "field": "urgency",
                      "label": "紧急程度",
                      "type": "select",
                      "required": true,
                      "options": [
                        {"label": "普通", "value": "normal"},
                        {"label": "紧急", "value": "urgent"},
                        {"label": "非常紧急", "value": "critical"}
                      ],
                      "default": "normal"
                    },
                    {
                      "field": "contractContent",
                      "label": "合同主要内容",
                      "type": "textarea",
                      "required": true,
                      "placeholder": "请简要描述合同的主要条款和内容"
                    },
                    {
                      "field": "specialTerms",
                      "label": "特殊条款说明",
                      "type": "textarea",
                      "required": false,
                      "placeholder": "如有特殊条款，请在此说明"
                    },
                    {
                      "field": "attachments",
                      "label": "合同附件",
                      "type": "file",
                      "required": true,
                      "multiple": true,
                      "accept": ".pdf,.doc,.docx"
                    },
                    {
                      "field": "remark",
                      "label": "备注说明",
                      "type": "textarea",
                      "required": false
                    }
                  ]
                }
                """;
    }
}
