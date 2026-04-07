package com.adminplus.utils;

import com.adminplus.pojo.dto.resp.DictItemResp;
import com.adminplus.pojo.entity.DictItemEntity;
import com.adminplus.repository.DictItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 字典工具类
 * 提供字典数据查询和转换功能
 *
 * @author AdminPlus
 * @since 2026-04-07
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DictUtils {

    private final DictItemRepository dictItemRepository;

    /**
     * 根据字典类型和值获取标签
     *
     * @param dictType 字典类型
     * @param value    字典值
     * @return 字典标签，未找到返回原值
     */
    @Cacheable(value = "dict:label", key = "#dictType + ':' + #value")
    public String getDictLabel(String dictType, String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        try {
            return dictItemRepository.findByDictTypeAndValue(dictType, value)
                    .map(DictItemEntity::getName)
                    .orElse(value);
        } catch (Exception e) {
            log.warn("获取字典标签失败: dictType={}, value={}", dictType, value, e);
            return value;
        }
    }

    /**
     * 根据字典类型获取所有字典项的 Map（value -> label）
     *
     * @param dictType 字典类型
     * @return 字典项映射
     */
    @Cacheable(value = "dict:map", key = "#dictType")
    public Map<String, String> getDictMap(String dictType) {
        try {
            List<DictItemEntity> items = dictItemRepository.findByDictType(dictType);
            return items.stream()
                    .collect(Collectors.toMap(
                            DictItemEntity::getValue,
                            DictItemEntity::getName,
                            (existing, replacement) -> existing
                    ));
        } catch (Exception e) {
            log.warn("获取字典映射失败: dictType={}", dictType, e);
            return Collections.emptyMap();
        }
    }

    /**
     * 根据字典类型获取所有字典项
     *
     * @param dictType 字典类型
     * @return 字典项列表
     */
    @Cacheable(value = "dict:items", key = "#dictType")
    public List<DictItemResp> getDictItems(String dictType) {
        try {
            List<DictItemEntity> entities = dictItemRepository.findByDictType(dictType);
            return entities.stream()
                    .map(this::toResp)
                    .toList();
        } catch (Exception e) {
            log.warn("获取字典项失败: dictType={}", dictType, e);
            return Collections.emptyList();
        }
    }

    /**
     * 解析字典值，返回对应的标签
     *
     * @param dictType 字典类型
     * @param value    字典值（支持多个，逗号分隔）
     * @return 字典标签（多个用逗号分隔）
     */
    public String parseDictValue(String dictType, String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        if (value.contains(",")) {
            return Arrays.stream(value.split(","))
                    .map(v -> getDictLabel(dictType, v.trim()))
                    .collect(Collectors.joining(","));
        }
        return getDictLabel(dictType, value);
    }

    /**
     * 反向解析：根据标签获取值
     *
     * @param dictType 字典类型
     * @param label    字典标签
     * @return 字典值，未找到返回 null
     */
    public String getDictValue(String dictType, String label) {
        if (label == null || label.isEmpty()) {
            return null;
        }
        try {
            return dictItemRepository.findByDictTypeAndName(dictType, label)
                    .map(DictItemEntity::getValue)
                    .orElse(null);
        } catch (Exception e) {
            log.warn("获取字典值失败: dictType={}, label={}", dictType, label, e);
            return null;
        }
    }

    private DictItemResp toResp(DictItemEntity entity) {
        return new DictItemResp(
                entity.getId(),
                entity.getDictId(),
                null, // dictType - not needed for simple response
                entity.getParentId(),
                entity.getName(),
                entity.getValue(),
                entity.getSortOrder(),
                entity.getStatus(),
                entity.getRemark(),
                null, // children - not needed for simple response
                entity.getCreateTime(),
                entity.getUpdateTime()
        );
    }
}
