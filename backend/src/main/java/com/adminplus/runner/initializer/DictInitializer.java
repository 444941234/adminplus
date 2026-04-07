package com.adminplus.runner.initializer;

import com.adminplus.pojo.entity.DictEntity;
import com.adminplus.pojo.entity.DictItemEntity;
import com.adminplus.repository.DictRepository;
import com.adminplus.repository.DictItemRepository;
import com.adminplus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 字典数据初始化器
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DictInitializer implements DataInitializer {

    private final DictRepository dictRepository;
    private final DictItemRepository dictItemRepository;
    private final UserRepository userRepository;

    @Override
    public int getOrder() {
        return 6;
    }

    @Override
    public String getName() {
        return "字典数据初始化";
    }

    @Override
    @Transactional
    public void initialize() {
        if (dictRepository.count() > 0) {
            log.info("字典数据已存在，跳过初始化");
            return;
        }

        // 获取 admin 用户 ID
        String adminUserId = userRepository.findByUsername("admin")
                .map(u -> u.getId())
                .orElse("system");

        List<DictEntity> dicts = new ArrayList<>();

        // 性别字典
        DictEntity genderDict = createDict("gender", "性别", "用户性别字典", adminUserId);
        dicts.add(genderDict);

        // 通用状态字典
        DictEntity statusDict = createDict("common_status", "通用状态", "通用状态字典（正常/禁用）", adminUserId);
        dicts.add(statusDict);

        // 菜单类型字典
        DictEntity menuTypeDict = createDict("menu_type", "菜单类型", "菜单类型字典", adminUserId);
        dicts.add(menuTypeDict);

        // 操作类型字典
        DictEntity operationTypeDict = createDict("operation_type", "操作类型", "日志操作类型字典", adminUserId);
        dicts.add(operationTypeDict);

        // 日志类型字典
        DictEntity logTypeDict = createDict("log_type", "日志类型", "日志分类字典", adminUserId);
        dicts.add(logTypeDict);

        // 日志状态字典
        DictEntity logStatusDict = createDict("log_status", "日志状态", "日志执行状态字典", adminUserId);
        dicts.add(logStatusDict);

        // 用户状态字典
        DictEntity userStatusDict = createDict("user_status", "用户状态", "用户账户状态字典", adminUserId);
        dicts.add(userStatusDict);

        // 工作流状态字典
        DictEntity workflowStatusDict = createDict("workflow_status", "工作流状态", "工作流转状态字典", adminUserId);
        dicts.add(workflowStatusDict);

        // 审批状态字典
        DictEntity approvalStatusDict = createDict("approval_status", "审批状态", "审批节点状态字典", adminUserId);
        dicts.add(approvalStatusDict);

        // 存储类型字典
        DictEntity storageTypeDict = createDict("storage_type", "存储类型", "文件存储方式字典", adminUserId);
        dicts.add(storageTypeDict);

        // 通知类型字典
        DictEntity noticeTypeDict = createDict("notice_type", "通知类型", "消息通知分类字典", adminUserId);
        dicts.add(noticeTypeDict);

        // 先保存字典以生成 ID
        List<DictEntity> savedDicts = dictRepository.saveAll(dicts);

        // 用保存后的字典（有 ID）创建字典项
        List<DictItemEntity> items = new ArrayList<>();
        for (DictEntity dict : savedDicts) {
            String dictType = dict.getDictType();
            switch (dictType) {
                // 性别字典
                case "gender":
                    items.add(createDictItem(dict, "男", "1", 1, adminUserId));
                    items.add(createDictItem(dict, "女", "2", 2, adminUserId));
                    items.add(createDictItem(dict, "保密", "0", 3, adminUserId));
                    break;

                // 通用状态字典
                case "common_status":
                    items.add(createDictItem(dict, "正常", "1", 1, adminUserId));
                    items.add(createDictItem(dict, "禁用", "0", 2, adminUserId));
                    break;

                // 菜单类型字典
                case "menu_type":
                    items.add(createDictItem(dict, "目录", "0", 1, adminUserId));
                    items.add(createDictItem(dict, "菜单", "1", 2, adminUserId));
                    items.add(createDictItem(dict, "按钮", "2", 3, adminUserId));
                    break;

                // 操作类型字典
                case "operation_type":
                    items.add(createDictItem(dict, "查询", "1", 1, adminUserId));
                    items.add(createDictItem(dict, "新增", "2", 2, adminUserId));
                    items.add(createDictItem(dict, "修改", "3", 3, adminUserId));
                    items.add(createDictItem(dict, "删除", "4", 4, adminUserId));
                    items.add(createDictItem(dict, "导出", "5", 5, adminUserId));
                    items.add(createDictItem(dict, "导入", "6", 6, adminUserId));
                    items.add(createDictItem(dict, "其他", "7", 7, adminUserId));
                    break;

                // 日志类型字典
                case "log_type":
                    items.add(createDictItem(dict, "操作日志", "1", 1, adminUserId));
                    items.add(createDictItem(dict, "登录日志", "2", 2, adminUserId));
                    items.add(createDictItem(dict, "系统日志", "3", 3, adminUserId));
                    break;

                // 日志状态字典
                case "log_status":
                    items.add(createDictItem(dict, "成功", "1", 1, adminUserId));
                    items.add(createDictItem(dict, "失败", "0", 2, adminUserId));
                    break;

                // 用户状态字典
                case "user_status":
                    items.add(createDictItem(dict, "正常", "1", 1, adminUserId));
                    items.add(createDictItem(dict, "禁用", "0", 2, adminUserId));
                    items.add(createDictItem(dict, "锁定", "2", 3, adminUserId));
                    break;

                // 工作流状态字典
                case "workflow_status":
                    items.add(createDictItem(dict, "草稿", "0", 1, adminUserId));
                    items.add(createDictItem(dict, "运行中", "1", 2, adminUserId));
                    items.add(createDictItem(dict, "已完成", "2", 3, adminUserId));
                    items.add(createDictItem(dict, "已拒绝", "3", 4, adminUserId));
                    items.add(createDictItem(dict, "已撤回", "4", 5, adminUserId));
                    break;

                // 审批状态字典
                case "approval_status":
                    items.add(createDictItem(dict, "待审批", "0", 1, adminUserId));
                    items.add(createDictItem(dict, "已通过", "1", 2, adminUserId));
                    items.add(createDictItem(dict, "已拒绝", "2", 3, adminUserId));
                    items.add(createDictItem(dict, "已撤回", "3", 4, adminUserId));
                    break;

                // 存储类型字典
                case "storage_type":
                    items.add(createDictItem(dict, "本地存储", "1", 1, adminUserId));
                    items.add(createDictItem(dict, "OSS存储", "2", 2, adminUserId));
                    items.add(createDictItem(dict, "数据库", "3", 3, adminUserId));
                    break;

                // 通知类型字典
                case "notice_type":
                    items.add(createDictItem(dict, "系统通知", "1", 1, adminUserId));
                    items.add(createDictItem(dict, "审批通知", "2", 2, adminUserId));
                    items.add(createDictItem(dict, "待办通知", "3", 3, adminUserId));
                    break;
            }
        }

        dictItemRepository.saveAll(items);
        log.info("初始化字典数据完成，共 {} 个字典，{} 个字典项", savedDicts.size(), items.size());
    }

    private DictEntity createDict(String dictType, String dictName, String remark, String userId) {
        DictEntity dict = new DictEntity();
        dict.setDictType(dictType);
        dict.setDictName(dictName);
        dict.setRemark(remark);
        dict.setStatus(1);
        dict.setCreateUser(userId);
        dict.setUpdateUser(userId);
        return dict;
    }

    private DictItemEntity createDictItem(DictEntity dict, String label, String value, Integer sortOrder, String userId) {
        DictItemEntity item = new DictItemEntity();
        item.setDictId(dict.getId());
        item.setName(label);
        item.setValue(value);
        item.setSortOrder(sortOrder);
        item.setStatus(1);
        item.setCreateUser(userId);
        item.setUpdateUser(userId);
        return item;
    }
}