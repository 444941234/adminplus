package com.adminplus.runner.initializer;

import com.adminplus.pojo.entity.FormTemplateEntity;
import com.adminplus.repository.FormTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 表单模板数据初始化器
 * <p>
 * 初始化常用的表单模板，如请假申请、报销申请等
 * </p>
 *
 * @author AdminPlus
 * @since 2026-04-02
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FormTemplateInitializer implements DataInitializer {

    private final FormTemplateRepository formTemplateRepository;

    @Override
    public int getOrder() {
        return 6; // 在工作流定义之前初始化
    }

    @Override
    public String getName() {
        return "表单模板数据初始化";
    }

    @Override
    @Transactional
    public void initialize() {
        long templateCount = formTemplateRepository.count();

        if (templateCount > 0) {
            log.info("表单模板数据已存在（{} 个模板），跳过初始化", templateCount);
            return;
        }

        log.info("开始初始化表单模板数据");

        // 创建通用表单模板
        createTemplate(
                "请假申请表",
                "leave_request",
                "人事审批",
                "员工请假申请表单，包含请假类型、时间、原因等信息",
                buildLeaveRequestFormConfig()
        );

        createTemplate(
                "报销申请表",
                "expense_claim",
                "财务审批",
                "费用报销申请表单，包含报销类型、金额、明细等信息",
                buildExpenseClaimFormConfig()
        );

        createTemplate(
                "采购申请表",
                "purchase_request",
                "采购审批",
                "物品采购申请表单，包含物品信息、数量、预算等信息",
                buildPurchaseRequestFormConfig()
        );

        createTemplate(
                "合同审批表",
                "contract_approval",
                "法务审批",
                "合同审批表单，包含合同类型、金额、对方单位等信息",
                buildContractApprovalFormConfig()
        );

        log.info("表单模板数据初始化完成，共创建 4 个模板");
    }

    private void createTemplate(String name, String code, String category, String description, String formConfig) {
        FormTemplateEntity template = new FormTemplateEntity();
        template.setTemplateName(name);
        template.setTemplateCode(code);
        template.setCategory(category);
        template.setDescription(description);
        template.setFormConfig(formConfig);
        template.setStatus(1);
        formTemplateRepository.save(template);
        log.info("创建表单模板: {} ({})", name, code);
    }

    private String buildLeaveRequestFormConfig() {
        return """
                {
                  "sections": [
                    {
                      "key": "basic_info",
                      "title": "基本信息",
                      "fields": [
                        {
                          "field": "leaveType",
                          "label": "请假类型",
                          "component": "select",
                          "required": true,
                          "options": [
                            {"label": "事假", "value": "personal"},
                            {"label": "病假", "value": "sick"},
                            {"label": "年假", "value": "annual"},
                            {"label": "调休", "value": "comp_time"}
                          ]
                        },
                        {
                          "field": "startDate",
                          "label": "开始时间",
                          "component": "date",
                          "required": true
                        },
                        {
                          "field": "endDate",
                          "label": "结束时间",
                          "component": "date",
                          "required": true
                        },
                        {
                          "field": "leaveDays",
                          "label": "请假天数",
                          "component": "number",
                          "required": true,
                          "rules": {"min": 0.5, "max": 365}
                        }
                      ]
                    },
                    {
                      "key": "reason",
                      "title": "请假原因",
                      "fields": [
                        {
                          "field": "leaveReason",
                          "label": "请假事由",
                          "component": "textarea",
                          "required": true,
                          "rules": {"min": 5, "max": 500}
                        },
                        {
                          "field": "attachment",
                          "label": "相关附件",
                          "component": "file",
                          "description": "如病假需上传证明材料"
                        }
                      ]
                    }
                  ]
                }
                """;
    }

    private String buildExpenseClaimFormConfig() {
        return """
                {
                  "sections": [
                    {
                      "key": "expense_info",
                      "title": "报销信息",
                      "fields": [
                        {
                          "field": "expenseType",
                          "label": "报销类型",
                          "component": "select",
                          "required": true,
                          "options": [
                            {"label": "差旅费", "value": "travel"},
                            {"label": "交通费", "value": "transport"},
                            {"label": "招待费", "value": "entertainment"},
                            {"label": "办公费", "value": "office"},
                            {"label": "其他", "value": "other"}
                          ]
                        },
                        {
                          "field": "expenseAmount",
                          "label": "报销金额",
                          "component": "number",
                          "required": true,
                          "rules": {"min": 0.01}
                        },
                        {
                          "field": "expenseDate",
                          "label": "费用发生日期",
                          "component": "date",
                          "required": true
                        }
                      ]
                    },
                    {
                      "key": "description",
                      "title": "费用说明",
                      "fields": [
                        {
                          "field": "expenseDescription",
                          "label": "费用明细说明",
                          "component": "textarea",
                          "required": true
                        },
                        {
                          "field": "invoice",
                          "label": "发票附件",
                          "component": "file",
                          "required": true
                        }
                      ]
                    }
                  ]
                }
                """;
    }

    private String buildPurchaseRequestFormConfig() {
        return """
                {
                  "sections": [
                    {
                      "key": "item_info",
                      "title": "物品信息",
                      "fields": [
                        {
                          "field": "itemName",
                          "label": "物品名称",
                          "component": "input",
                          "required": true
                        },
                        {
                          "field": "itemSpec",
                          "label": "规格型号",
                          "component": "input"
                        },
                        {
                          "field": "quantity",
                          "label": "数量",
                          "component": "number",
                          "required": true,
                          "rules": {"min": 1}
                        },
                        {
                          "field": "unitPrice",
                          "label": "单价",
                          "component": "number",
                          "required": true,
                          "rules": {"min": 0.01}
                        }
                      ]
                    },
                    {
                      "key": "supplier_info",
                      "title": "供应商信息",
                      "fields": [
                        {
                          "field": "supplierName",
                          "label": "供应商名称",
                          "component": "input",
                          "required": true
                        },
                        {
                          "field": "expectedDeliveryDate",
                          "label": "期望交付日期",
                          "component": "date",
                          "required": true
                        }
                      ]
                    },
                    {
                      "key": "reason",
                      "title": "申请原因",
                      "fields": [
                        {
                          "field": "purchaseReason",
                          "label": "采购原因说明",
                          "component": "textarea",
                          "required": true
                        }
                      ]
                    }
                  ]
                }
                """;
    }

    private String buildContractApprovalFormConfig() {
        return """
                {
                  "sections": [
                    {
                      "key": "contract_info",
                      "title": "合同基本信息",
                      "fields": [
                        {
                          "field": "contractName",
                          "label": "合同名称",
                          "component": "input",
                          "required": true
                        },
                        {
                          "field": "contractType",
                          "label": "合同类型",
                          "component": "select",
                          "required": true,
                          "options": [
                            {"label": "销售合同", "value": "sales"},
                            {"label": "采购合同", "value": "purchase"},
                            {"label": "服务合同", "value": "service"},
                            {"label": "租赁合同", "value": "lease"},
                            {"label": "其他", "value": "other"}
                          ]
                        },
                        {
                          "field": "contractAmount",
                          "label": "合同金额",
                          "component": "number",
                          "required": true,
                          "rules": {"min": 0}
                        },
                        {
                          "field": "counterparty",
                          "label": "对方单位",
                          "component": "input",
                          "required": true
                        }
                      ]
                    },
                    {
                      "key": "dates",
                      "title": "合同期限",
                      "fields": [
                        {
                          "field": "contractStartDate",
                          "label": "开始日期",
                          "component": "date",
                          "required": true
                        },
                        {
                          "field": "contractEndDate",
                          "label": "结束日期",
                          "component": "date",
                          "required": true
                        }
                      ]
                    },
                    {
                      "key": "attachments",
                      "title": "合同附件",
                      "fields": [
                        {
                          "field": "contractFile",
                          "label": "合同文件",
                          "component": "file",
                          "required": true,
                          "description": "请上传合同正本扫描件"
                        },
                        {
                          "field": "remarks",
                          "label": "备注说明",
                          "component": "textarea"
                        }
                      ]
                    }
                  ]
                }
                """;
    }
}
