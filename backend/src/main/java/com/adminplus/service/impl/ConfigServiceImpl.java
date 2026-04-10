package com.adminplus.service.impl;

import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.dto.query.ConfigQuery;
import com.adminplus.pojo.dto.request.*;
import com.adminplus.pojo.dto.response.*;
import com.adminplus.pojo.dto.response.PageResultResponse;
import com.adminplus.pojo.entity.ConfigEntity;
import com.adminplus.pojo.entity.ConfigGroupEntity;
import com.adminplus.pojo.entity.ConfigHistoryEntity;
import com.adminplus.repository.ConfigGroupRepository;
import com.adminplus.repository.ConfigHistoryRepository;
import com.adminplus.repository.ConfigRepository;
import com.adminplus.service.ConfigService;
import com.adminplus.utils.EntityHelper;
import com.adminplus.utils.PageUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 配置服务实现
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final ConfigRepository configRepository;
    private final ConfigGroupRepository configGroupRepository;
    private final ConfigHistoryRepository configHistoryRepository;
    private final JsonMapper objectMapper;
    private final ConversionService conversionService;

    @Override
    @Transactional(readOnly = true)
    public PageResultResponse<ConfigResponse> getConfigList(ConfigQuery query) {
        Pageable pageable = PageUtils.toPageableAsc(query.getPage(), query.getSize(), "sortOrder");

        Specification<ConfigEntity> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query.getGroupId() != null && !query.getGroupId().isEmpty()) {
                predicates.add(cb.equal(root.get("groupId"), query.getGroupId()));
            }

            if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
                predicates.add(cb.or(
                        cb.like(root.get("name"), "%" + query.getKeyword() + "%"),
                        cb.like(root.get("key"), "%" + query.getKeyword() + "%")
                ));
            }

            if (query.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), query.getStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        var pageResult = configRepository.findAll(spec, pageable);
        List<ConfigEntity> configs = pageResult.getContent();

        // Batch fetch group names to avoid N+1 queries
        Map<String, String> groupNameMap = batchGetGroupNames(configs);

        return PageResultResponse.from(pageResult, config -> toResponseWithGroupName(config, groupNameMap.get(config.getGroupId())));
    }

    /**
     * Batch fetch group names for configs
     */
    private Map<String, String> batchGetGroupNames(List<ConfigEntity> configs) {
        List<String> groupIds = configs.stream()
                .map(ConfigEntity::getGroupId)
                .filter(id -> id != null && !id.isEmpty())
                .distinct()
                .toList();

        if (groupIds.isEmpty()) {
            return Map.of();
        }

        return configGroupRepository.findAllById(groupIds).stream()
                .collect(Collectors.toMap(ConfigGroupEntity::getId, ConfigGroupEntity::getName));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "config", key = "'id:' + #id")
    public ConfigResponse getConfigById(String id) {
        ConfigEntity config = EntityHelper.findByIdOrThrow(
                configRepository::findById, id, "配置不存在"
        );
        return conversionService.convert(config, ConfigResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "config", key = "'key:' + #key")
    public ConfigResponse getConfigByKey(String key) {
        ConfigEntity config = configRepository.findByKey(key)
                .orElseThrow(() -> new BizException("配置不存在"));
        return conversionService.convert(config, ConfigResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConfigResponse> getConfigsByGroupId(String groupId) {
        // 验证配置组是否存在
        ConfigGroupEntity group = EntityHelper.findByIdOrThrow(
                configGroupRepository::findById, groupId, "配置组不存在"
        );

        return configRepository.findByGroupIdOrderBySortOrderAsc(groupId).stream()
                .map(config -> conversionService.convert(config, ConfigResponse.class))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "config", key = "'groupCode:' + #groupCode")
    public List<ConfigResponse> getConfigsByGroupCode(String groupCode) {
        // 根据编码查找配置组
        ConfigGroupEntity group = configGroupRepository.findByCode(groupCode)
                .orElseThrow(() -> new BizException("配置组不存在"));

        return configRepository.findByGroupIdOrderBySortOrderAsc(group.getId()).stream()
                .map(config -> conversionService.convert(config, ConfigResponse.class))
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = "config", allEntries = true)
    public ConfigResponse createConfig(ConfigCreateRequest request) {
        // 检查配置键是否已存在
        if (configRepository.existsByKey(request.key())) {
            throw new BizException("配置键已存在");
        }

        // 验证配置组是否存在
        ConfigGroupEntity group = EntityHelper.findByIdOrThrow(
                configGroupRepository::findById, request.groupId(), "配置组不存在"
        );

        // 验证配置值
        validateConfigValue(request.valueType(), request.value(), request.validationRule());

        ConfigEntity config = new ConfigEntity();
        config.setGroupId(request.groupId());
        config.setName(request.name());
        config.setKey(request.key());
        config.setValue(request.value());
        config.setValueType(request.valueType());
        config.setEffectType(request.effectType() != null ? request.effectType() : "IMMEDIATE");
        config.setDefaultValue(request.defaultValue());
        config.setDescription(request.description());
        config.setIsRequired(request.isRequired() != null ? request.isRequired() : false);
        config.setValidationRule(request.validationRule());
        config.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);
        config.setStatus(1); // 默认启用

        config = configRepository.save(config);

        // 记录历史
        saveHistory(config, null, config.getValue(), "创建");

        log.info("创建配置成功: {}", config.getKey());

        return conversionService.convert(config, ConfigResponse.class);
    }

    @Override
    @Transactional
    @CacheEvict(value = "config", allEntries = true)
    public ConfigResponse updateConfig(String id, ConfigUpdateRequest request) {
        ConfigEntity config = EntityHelper.findByIdOrThrow(
                configRepository::findById, id, "配置不存在"
        );

        // 保存旧值用于历史记录
        String oldValue = config.getValue();

        // 更新字段
        if (request.name() != null) {
            config.setName(request.name());
        }
        if (request.value() != null) {
            // 验证配置值
            String valueType = request.valueType() != null ? request.valueType() : config.getValueType();
            String validationRule = request.validationRule() != null ? request.validationRule() : config.getValidationRule();
            validateConfigValue(valueType, request.value(), validationRule);

            config.setValue(request.value());
        }
        if (request.valueType() != null) {
            config.setValueType(request.valueType());
        }
        if (request.effectType() != null) {
            config.setEffectType(request.effectType());
        }
        if (request.defaultValue() != null) {
            config.setDefaultValue(request.defaultValue());
        }
        if (request.description() != null) {
            config.setDescription(request.description());
        }
        if (request.isRequired() != null) {
            config.setIsRequired(request.isRequired());
        }
        if (request.validationRule() != null) {
            config.setValidationRule(request.validationRule());
        }
        if (request.sortOrder() != null) {
            config.setSortOrder(request.sortOrder());
        }
        if (request.status() != null) {
            config.setStatus(request.status());
        }

        config = configRepository.save(config);

        // 如果值发生变化，记录历史
        if (request.value() != null && !request.value().equals(oldValue)) {
            saveHistory(config, oldValue, config.getValue(), "更新值");
        }

        log.info("更新配置成功: {}", config.getKey());

        return conversionService.convert(config, ConfigResponse.class);
    }

    @Override
    @Transactional
    @CacheEvict(value = "config", allEntries = true)
    public void deleteConfig(String id) {
        ConfigEntity config = EntityHelper.findByIdOrThrow(
                configRepository::findById, id, "配置不存在"
        );

        configRepository.deleteById(id);
        log.info("删除配置成功: {}", config.getKey());
    }

    @Override
    @Transactional
    @CacheEvict(value = "config", allEntries = true)
    public void updateConfigStatus(String id, Integer status) {
        ConfigEntity config = EntityHelper.findByIdOrThrow(
                configRepository::findById, id, "配置不存在"
        );

        config.setStatus(status);
        configRepository.save(config);
        log.info("更新配置状态成功: {} -> {}", config.getKey(), status);
    }

    @Override
    @Transactional
    @CacheEvict(value = "config", allEntries = true)
    public ConfigImportResultResponse batchUpdateConfigs(ConfigBatchUpdateRequest request) {
        int total = request.items().size();
        int success = 0;
        int skipped = 0;
        int failed = 0;
        List<ConfigImportResultResponse.ImportDetail> details = new ArrayList<>();

        for (ConfigBatchUpdateRequest.ConfigItemUpdate item : request.items()) {
            try {
                ConfigEntity config = EntityHelper.findByIdOrThrow(
                        configRepository::findById, item.id(), "配置不存在: " + item.id()
                );

                String oldValue = config.getValue();
                config.setValue(item.value());
                configRepository.save(config);

                // 记录历史
                saveHistory(config, oldValue, item.value(), "批量更新");

                success++;
                details.add(new ConfigImportResultResponse.ImportDetail(
                        config.getKey(),
                        "success",
                        null
                ));

                log.info("批量更新配置成功: {} = {}", config.getKey(), item.value());

            } catch (Exception e) {
                failed++;
                details.add(new ConfigImportResultResponse.ImportDetail(
                        item.id(),
                        "failed",
                        e.getMessage()
                ));
                log.error("批量更新配置失败: {}", item.id(), e);
            }
        }

        return new ConfigImportResultResponse(total, success, skipped, failed, details);
    }

    @Override
    @Transactional(readOnly = true)
    public ConfigExportResponse exportConfigs(List<String> groupIds) {
        List<ConfigGroupEntity> groups;
        if (groupIds == null || groupIds.isEmpty()) {
            groups = configGroupRepository.findByStatusOrderBySortOrderAsc(1);
        } else {
            groups = groupIds.stream()
                    .map(id -> EntityHelper.findByIdOrThrow(
                            configGroupRepository::findById, id, "配置组不存在: " + id
                    ))
                    .sorted(Comparator.comparingInt(ConfigGroupEntity::getSortOrder))
                    .toList();
        }

        List<ConfigExportResponse.ExportGroup> exportGroups = groups.stream()
                .map(group -> {
                    List<ConfigEntity> configs = configRepository
                            .findByGroupIdAndStatusOrderBySortOrderAsc(group.getId(), 1);
                    List<ConfigExportResponse.ExportConfig> exportConfigs = configs.stream()
                            .map(config -> new ConfigExportResponse.ExportConfig(
                                    config.getKey(),
                                    config.getName(),
                                    config.getValue(),
                                    config.getValueType(),
                                    config.getEffectType(),
                                    config.getDescription()
                            ))
                            .toList();
                    return new ConfigExportResponse.ExportGroup(
                            group.getCode(),
                            group.getName(),
                            group.getIcon(),
                            exportConfigs
                    );
                })
                .toList();

        String exportTime = Instant.now().atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return new ConfigExportResponse("1.0", exportTime, exportGroups);
    }

    @Override
    @Transactional
    @CacheEvict(value = "config", allEntries = true)
    public ConfigImportResultResponse importConfigs(ConfigImportRequest request) {
        // 简化实现：仅支持 JSON 格式
        if (!"JSON".equals(request.format())) {
            throw new BizException("暂仅支持 JSON 格式导入");
        }

        int total = 0;
        int success = 0;
        int skipped = 0;
        int failed = 0;
        List<ConfigImportResultResponse.ImportDetail> details = new ArrayList<>();

        try {
            Map<String, Object> importData = objectMapper.readValue(
                    request.content(), new TypeReference<>() {}
            );

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) importData.get("items");

            if (items != null) {
                total = items.size();

                for (Map<String, Object> item : items) {
                    try {
                        String key = (String) item.get("key");
                        String value = item.get("value") != null ? item.get("value").toString() : null;

                        ConfigEntity config = configRepository.findByKey(key).orElse(null);

                        if (config == null) {
                            // 跳过不存在的配置
                            skipped++;
                            details.add(new ConfigImportResultResponse.ImportDetail(
                                    key,
                                    "skipped",
                                    "配置不存在"
                            ));
                            continue;
                        }

                        String oldValue = config.getValue();
                        config.setValue(value);
                        configRepository.save(config);

                        // 记录历史
                        saveHistory(config, oldValue, value, "导入");

                        success++;
                        details.add(new ConfigImportResultResponse.ImportDetail(
                                key,
                                "success",
                                null
                        ));

                        log.info("导入配置成功: {} = {}", key, value);

                    } catch (Exception e) {
                        failed++;
                        String key = (String) item.getOrDefault("key", "unknown");
                        details.add(new ConfigImportResultResponse.ImportDetail(
                                key,
                                "failed",
                                e.getMessage()
                        ));
                        log.error("导入配置失败: {}", item, e);
                    }
                }
            }

        } catch (JacksonException e) {
            throw new BizException("JSON 解析失败: " + e.getMessage());
        }

        return new ConfigImportResultResponse(total, success, skipped, failed, details);
    }

    @Override
    @Transactional
    @CacheEvict(value = "config", allEntries = true)
    public ConfigResponse rollbackConfig(String id, ConfigRollbackRequest request) {
        ConfigEntity config = EntityHelper.findByIdOrThrow(
                configRepository::findById, id, "配置不存在"
        );

        ConfigHistoryEntity history = EntityHelper.findByIdOrThrow(
                configHistoryRepository::findById, request.historyId(), "历史记录不存在"
        );

        if (!history.getConfigId().equals(id)) {
            throw new BizException("历史记录与配置不匹配");
        }

        // 回滚值
        String oldValue = config.getValue();
        config.setValue(history.getOldValue());
        configRepository.save(config);

        // 记录回滚历史
        saveHistory(config, oldValue, history.getOldValue(), "回滚到版本: " + history.getId());

        log.info("回滚配置成功: {} -> {}", config.getKey(), history.getOldValue());

        return conversionService.convert(config, ConfigResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConfigHistoryResponse> getConfigHistory(String id) {
        ConfigEntity config = EntityHelper.findByIdOrThrow(
                configRepository::findById, id, "配置不存在"
        );

        return configHistoryRepository.findByConfigIdOrderByCreateTimeDesc(id).stream()
                .map(h -> conversionService.convert(h, ConfigHistoryResponse.class))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ConfigEffectInfoResponse getConfigEffectInfo() {
        // 查询所有需要手动生效的配置
        List<ConfigEntity> manualConfigs = configRepository.findByEffectType("MANUAL");

        List<ConfigEffectInfoResponse.PendingEffect> pendingEffects = manualConfigs.stream()
                .filter(c -> c.getStatus() == 1)
                .map(c -> new ConfigEffectInfoResponse.PendingEffect(
                        c.getKey(),
                        c.getName(),
                        c.getValue(),
                        c.getEffectType(),
                        c.getUpdateTime().atZone(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                ))
                .toList();

        // 查询需要重启的配置
        List<ConfigEntity> restartConfigs = configRepository.findByEffectType("RESTART");
        List<String> restartRequired = restartConfigs.stream()
                .filter(c -> c.getStatus() == 1)
                .map(ConfigEntity::getKey)
                .toList();

        return new ConfigEffectInfoResponse(pendingEffects, restartRequired);
    }

    @Override
    @Transactional
    @CacheEvict(value = "config", allEntries = true)
    public void applyConfig(String id) {
        ConfigEntity config = EntityHelper.findByIdOrThrow(
                configRepository::findById, id, "配置不存在"
        );

        if (!"MANUAL".equals(config.getEffectType())) {
            throw new BizException("该配置不是手动生效类型");
        }

        // 手动生效：这里只是标记，实际生效逻辑由业务系统处理
        String currentValue = config.getValue();
        saveHistory(config, currentValue, currentValue, "手动生效");

        log.info("手动生效配置成功: {}", config.getKey());
    }

    @Override
    @CacheEvict(value = {"config", "configGroups", "configByKey"}, allEntries = true)
    public void refreshConfigCache() {
        log.info("刷新配置缓存成功");
    }

    /**
     * 保存配置历史
     */
    private void saveHistory(ConfigEntity config, String oldValue, String newValue, String operation) {
        ConfigHistoryEntity history = new ConfigHistoryEntity();
        history.setConfigId(config.getId());
        history.setConfigKey(config.getKey());
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        history.setRemark("操作: " + operation);

        configHistoryRepository.save(history);
    }

    /**
     * 转换实体为响应对象（带预先查询的组名）
     * 用于批量查询场景，避免 N+1 问题
     */
    private ConfigResponse toResponseWithGroupName(ConfigEntity entity, String groupName) {
        return new ConfigResponse(
                entity.getId(),
                entity.getGroupId(),
                groupName,
                entity.getName(),
                entity.getKey(),
                entity.getValue(),
                entity.getValueType(),
                entity.getEffectType(),
                entity.getDefaultValue(),
                entity.getDescription(),
                entity.getIsRequired(),
                entity.getValidationRule(),
                entity.getSortOrder(),
                entity.getStatus(),
                entity.getCreateTime(),
                entity.getUpdateTime()
        );
    }

    /**
     * 验证配置值
     */
    private void validateConfigValue(String valueType, String value, String validationRule) {
        if (value == null || value.isEmpty()) {
            return; // 空值跳过校验
        }

        switch (valueType) {
            case "NUMBER" -> {
                try {
                    Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    throw new BizException("配置值类型为数字时，值必须是有效的数字");
                }
            }
            case "BOOLEAN" -> {
                if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
                    throw new BizException("配置值类型为布尔值时，值必须是 true 或 false");
                }
            }
            case "JSON", "ARRAY" -> {
                try {
                    objectMapper.readTree(value);
                } catch (Exception e) {
                    throw new BizException("配置值格式不正确，必须是有效的JSON");
                }
            }
        }

        // 自定义校验规则
        if (validationRule != null && !validationRule.isEmpty()) {
            try {
                if (!value.matches(validationRule)) {
                    throw new BizException("配置值不符合校验规则: " + validationRule);
                }
            } catch (Exception e) {
                log.warn("校验规则执行失败: {}", validationRule, e);
            }
        }
    }
}
