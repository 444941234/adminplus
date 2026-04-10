package com.adminplus.service.impl;

import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.dto.query.ConfigGroupQuery;
import com.adminplus.pojo.dto.request.ConfigGroupCreateRequest;
import com.adminplus.pojo.dto.request.ConfigGroupUpdateRequest;
import com.adminplus.pojo.dto.response.ConfigGroupResponse;
import com.adminplus.pojo.dto.response.PageResultResponse;
import com.adminplus.pojo.entity.ConfigGroupEntity;
import com.adminplus.repository.ConfigGroupRepository;
import com.adminplus.repository.ConfigRepository;
import com.adminplus.service.ConfigGroupService;
import com.adminplus.utils.EntityHelper;
import com.adminplus.utils.PageUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置组服务实现
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigGroupServiceImpl implements ConfigGroupService {

    private final ConfigGroupRepository configGroupRepository;
    private final ConfigRepository configRepository;
    private final ConversionService conversionService;

    @Override
    @Transactional(readOnly = true)
    public PageResultResponse<ConfigGroupResponse> getConfigGroupList(ConfigGroupQuery query) {
        Pageable pageable = PageUtils.toPageableAsc(query.getPage(), query.getSize(), "sortOrder");

        Specification<ConfigGroupEntity> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
                predicates.add(cb.or(
                        cb.like(root.get("name"), "%" + query.getKeyword() + "%"),
                        cb.like(root.get("code"), "%" + query.getKeyword() + "%")
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        var pageResult = configGroupRepository.findAll(spec, pageable);
        return PageResultResponse.from(pageResult, this::toResponseWithConfigCount);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "configGroup", key = "'id:' + #id")
    public ConfigGroupResponse getConfigGroupById(String id) {
        ConfigGroupEntity group = EntityHelper.findByIdOrThrow(
                configGroupRepository::findById, id, "配置组不存在"
        );
        return toResponseWithConfigCount(group);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "configGroup", key = "'code:' + #code")
    public ConfigGroupResponse getConfigGroupByCode(String code) {
        ConfigGroupEntity group = configGroupRepository.findByCode(code)
                .orElseThrow(() -> new BizException("配置组不存在"));
        return toResponseWithConfigCount(group);
    }

    @Override
    @Transactional
    @CacheEvict(value = "configGroup", allEntries = true)
    public ConfigGroupResponse createConfigGroup(ConfigGroupCreateRequest request) {
        // 检查编码是否已存在
        if (configGroupRepository.existsByCode(request.code())) {
            throw new BizException("配置组编码已存在");
        }

        ConfigGroupEntity group = new ConfigGroupEntity();
        group.setName(request.name());
        group.setCode(request.code());
        group.setIcon(request.icon());
        group.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);
        group.setDescription(request.description());
        group.setStatus(1); // 默认启用

        group = configGroupRepository.save(group);

        return toResponseWithConfigCount(group);
    }

    @Override
    @Transactional
    @CacheEvict(value = "configGroup", allEntries = true)
    public ConfigGroupResponse updateConfigGroup(String id, ConfigGroupUpdateRequest req) {
        ConfigGroupEntity group = EntityHelper.findByIdOrThrow(
                configGroupRepository::findById, id, "配置组不存在"
        );

        if (req.name() != null) {
            group.setName(req.name());
        }
        if (req.icon() != null) {
            group.setIcon(req.icon());
        }
        if (req.sortOrder() != null) {
            group.setSortOrder(req.sortOrder());
        }
        if (req.description() != null) {
            group.setDescription(req.description());
        }

        group = configGroupRepository.save(group);

        return toResponseWithConfigCount(group);
    }

    @Override
    @Transactional
    @CacheEvict(value = "configGroup", allEntries = true)
    public void deleteConfigGroup(String id) {
        ConfigGroupEntity group = EntityHelper.findByIdOrThrow(
                configGroupRepository::findById, id, "配置组不存在"
        );

        // 检查是否有配置项
        long configCount = configRepository.countByGroupId(id);
        if (configCount > 0) {
            throw new BizException("该配置组下存在配置项，无法删除");
        }

        configGroupRepository.deleteById(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "configGroup", allEntries = true)
    public void updateConfigGroupStatus(String id, Integer status) {
        ConfigGroupEntity group = EntityHelper.findByIdOrThrow(
                configGroupRepository::findById, id, "配置组不存在"
        );

        group.setStatus(status);
        configGroupRepository.save(group);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "configGroup", key = "'active'")
    public List<ConfigGroupResponse> getActiveConfigGroups() {
        return configGroupRepository.findByStatusOrderBySortOrderAsc(1).stream()
                .map(this::toResponseWithConfigCount)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "configGroup", key = "'all'")
    public List<ConfigGroupResponse> getAllConfigGroups() {
        return configGroupRepository.findAll(Sort.by(Sort.Direction.ASC, "sortOrder")).stream()
                .map(this::toResponseWithConfigCount)
                .toList();
    }

    /**
     * 实体转换为响应对象，包含配置项数量
     */
    private ConfigGroupResponse toResponseWithConfigCount(ConfigGroupEntity entity) {
        long configCount = configRepository.countByGroupId(entity.getId());
        return new ConfigGroupResponse(
                entity.getId(),
                entity.getName(),
                entity.getCode(),
                entity.getIcon(),
                entity.getSortOrder(),
                entity.getDescription(),
                entity.getStatus(),
                configCount,
                entity.getCreateTime(),
                entity.getUpdateTime()
        );
    }
}
