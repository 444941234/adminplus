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

        // 状态字典
        DictEntity statusDict = createDict("status", "状态", "通用状态字典", adminUserId);
        dicts.add(statusDict);

        // 菜单类型字典
        DictEntity menuTypeDict = createDict("menu_type", "菜单类型", "菜单类型字典", adminUserId);
        dicts.add(menuTypeDict);

        // 先保存字典以生成 ID
        List<DictEntity> savedDicts = dictRepository.saveAll(dicts);

        // 用保存后的字典（有 ID）创建字典项
        List<DictItemEntity> items = new ArrayList<>();
        for (DictEntity dict : savedDicts) {
            if ("gender".equals(dict.getDictType())) {
                items.add(createDictItem(dict, "男", "1", 1, adminUserId));
                items.add(createDictItem(dict, "女", "2", 2, adminUserId));
            } else if ("status".equals(dict.getDictType())) {
                items.add(createDictItem(dict, "正常", "1", 1, adminUserId));
                items.add(createDictItem(dict, "禁用", "0", 2, adminUserId));
            } else if ("menu_type".equals(dict.getDictType())) {
                items.add(createDictItem(dict, "目录", "0", 1, adminUserId));
                items.add(createDictItem(dict, "菜单", "1", 2, adminUserId));
                items.add(createDictItem(dict, "按钮", "2", 3, adminUserId));
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