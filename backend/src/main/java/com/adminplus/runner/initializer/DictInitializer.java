package com.adminplus.runner.initializer;

import com.adminplus.pojo.entity.DictEntity;
import com.adminplus.pojo.entity.DictItemEntity;
import com.adminplus.repository.DictRepository;
import com.adminplus.repository.DictItemRepository;
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

        List<DictEntity> dicts = new ArrayList<>();
        List<DictItemEntity> items = new ArrayList<>();

        // 性别字典
        DictEntity genderDict = createDict("gender", "性别", "用户性别字典");
        dicts.add(genderDict);
        items.add(createDictItem(genderDict, "男", "1", 1));
        items.add(createDictItem(genderDict, "女", "2", 2));

        // 状态字典
        DictEntity statusDict = createDict("status", "状态", "通用状态字典");
        dicts.add(statusDict);
        items.add(createDictItem(statusDict, "正常", "1", 1));
        items.add(createDictItem(statusDict, "禁用", "0", 2));

        // 菜单类型字典
        DictEntity menuTypeDict = createDict("menu_type", "菜单类型", "菜单类型字典");
        dicts.add(menuTypeDict);
        items.add(createDictItem(menuTypeDict, "目录", "0", 1));
        items.add(createDictItem(menuTypeDict, "菜单", "1", 2));
        items.add(createDictItem(menuTypeDict, "按钮", "2", 3));

        dictRepository.saveAll(dicts);
        dictItemRepository.saveAll(items);
        log.info("初始化字典数据完成，共 {} 个字典，{} 个字典项", dicts.size(), items.size());
    }

    private DictEntity createDict(String dictType, String dictName, String remark) {
        DictEntity dict = new DictEntity();
        dict.setDictType(dictType);
        dict.setDictName(dictName);
        dict.setRemark(remark);
        dict.setStatus(1);
        return dict;
    }

    private DictItemEntity createDictItem(DictEntity dict, String label, String value, Integer sortOrder) {
        DictItemEntity item = new DictItemEntity();
        item.setDictId(dict.getId());
        item.setName(label);
        item.setValue(value);
        item.setSortOrder(sortOrder);
        item.setStatus(1);
        return item;
    }
}