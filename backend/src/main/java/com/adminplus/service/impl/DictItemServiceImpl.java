package com.adminplus.service.impl;

import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.dto.req.DictItemCreateReq;
import com.adminplus.pojo.dto.req.DictItemUpdateReq;
import com.adminplus.pojo.dto.resp.DictItemResp;
import com.adminplus.pojo.entity.DictEntity;
import com.adminplus.pojo.entity.DictItemEntity;
import com.adminplus.repository.DictItemRepository;
import com.adminplus.repository.DictRepository;
import com.adminplus.service.DictItemService;
import com.adminplus.utils.TreeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 字典项服务实现
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictItemServiceImpl implements DictItemService {

    private final DictItemRepository dictItemRepository;
    private final DictRepository dictRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "dictItem", key = "'dictId:' + #dictId")
    public List<DictItemResp> getDictItemsByDictId(String dictId) {
        DictEntity dict = dictRepository.findById(dictId).orElse(null);
        String dictType = dict != null ? dict.getDictType() : null;

        return dictItemRepository.findByDictIdOrderBySortOrderAsc(dictId).stream()
                .map(item -> toResp(item, dictType))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "dictItem", key = "'tree:dictId:' + #dictId")
    public List<DictItemResp> getDictItemTreeByDictId(String dictId) {
        List<DictItemEntity> items = dictItemRepository.findByDictIdOrderBySortOrderAsc(dictId);

        // 转换为 VO（扁平结构，children 为 null）
        List<DictItemResp> itemResps = items.stream()
                .map(item -> toResp(item, null))
                .toList();

        // 使用 TreeUtils.buildTreeForRecord 构建树形结构
        return TreeUtils.buildTreeForRecord(itemResps, this::createWithChildren);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "dictItem", key = "'type:' + #dictType")
    public List<DictItemResp> getDictItemsByType(String dictType) {
        DictEntity dict = dictRepository.findByDictType(dictType)
                .orElseThrow(() -> new BizException("字典不存在"));

        return dictItemRepository.findByDictIdAndStatusOrderBySortOrderAsc(dict.getId(), 1).stream()
                .map(item -> toRespWithDictType(item, dict.getDictType()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "dictItem", key = "'id:' + #id")
    public DictItemResp getDictItemById(String id) {
        DictItemEntity item = dictItemRepository.findById(id)
                .orElseThrow(() -> new BizException("字典项不存在"));

        DictEntity dict = dictRepository.findById(item.getDictId())
                .orElseThrow(() -> new BizException("字典不存在"));

        return toRespWithDictType(item, dict.getDictType());
    }

    @Override
    @Transactional
    @CacheEvict(value = "dictItem", allEntries = true)
    public DictItemResp createDictItem(DictItemCreateReq req) {
        DictEntity dict = dictRepository.findById(req.dictId())
                .orElseThrow(() -> new BizException("字典不存在"));

        var item = new DictItemEntity();
        item.setDictId(req.dictId());
        item.setLabel(req.label());
        item.setValue(req.value());
        item.setSortOrder(req.sortOrder() != null ? req.sortOrder() : 0);
        item.setStatus(req.status() != null ? req.status() : 1);
        item.setRemark(req.remark());

        // 设置父节点关系
        if (req.parentId() != null && !req.parentId().equals("0")) {
            DictItemEntity parent = dictItemRepository.findById(req.parentId())
                    .orElseThrow(() -> new BizException("父节点不存在"));
            if (!parent.getDictId().equals(req.dictId())) {
                throw new BizException("父节点不属于当前字典");
            }
            item.setParent(parent);
            // 更新 ancestors
            String parentAncestors = parent.getAncestors() != null ? parent.getAncestors() : "";
            item.setAncestors(parentAncestors + parent.getId() + ",");
        } else {
            item.setAncestors("0,");
        }

        item = dictItemRepository.save(item);
        log.info("创建字典项成功: {} - {}", dict.getDictType(), item.getLabel());
        return toRespWithDictType(item, dict.getDictType());
    }

    @Override
    @Transactional
    @CacheEvict(value = "dictItem", allEntries = true)
    public DictItemResp updateDictItem(String id, DictItemUpdateReq req) {
        DictItemEntity item = dictItemRepository.findById(id)
                .orElseThrow(() -> new BizException("字典项不存在"));

        DictEntity dict = dictRepository.findById(item.getDictId())
                .orElseThrow(() -> new BizException("字典不存在"));

        // 验证并更新父节点
        if (req.getParentId().isPresent()) {
            String newParentId = req.getParentId().get();
            if (!id.equals(newParentId)) {
                if (newParentId != null && !newParentId.equals("0")) {
                    DictItemEntity parent = dictItemRepository.findById(newParentId)
                            .orElseThrow(() -> new BizException("父节点不存在"));
                    if (!parent.getDictId().equals(item.getDictId())) {
                        throw new BizException("父节点不属于当前字典");
                    }
                    // 防止循环引用
                    if (isCircularReference(newParentId, id, item.getDictId())) {
                        throw new BizException("不能将父节点设置为自己的子节点");
                    }
                    item.setParent(parent);
                    // 更新 ancestors
                    String parentAncestors = parent.getAncestors() != null ? parent.getAncestors() : "";
                    item.setAncestors(parentAncestors + parent.getId() + ",");
                } else {
                    item.setParent(null);
                    item.setAncestors("0,");
                }
            }
        }

        req.getLabel().ifPresent(item::setLabel);
        req.getValue().ifPresent(item::setValue);
        req.getSortOrder().ifPresent(item::setSortOrder);
        req.getStatus().ifPresent(item::setStatus);
        req.getRemark().ifPresent(item::setRemark);

        item = dictItemRepository.save(item);
        log.info("更新字典项成功: {} - {}", dict.getDictType(), item.getLabel());
        return toRespWithDictType(item, dict.getDictType());
    }

    @Override
    @Transactional
    @CacheEvict(value = "dictItem", allEntries = true)
    public void deleteDictItem(String id) {
        DictItemEntity item = dictItemRepository.findById(id)
                .orElseThrow(() -> new BizException("字典项不存在"));

        // 检查是否有子节点
        if (!item.getChildren().isEmpty()) {
            throw new BizException("该字典项下存在子节点，无法删除");
        }

        dictItemRepository.delete(item);
        log.info("删除字典项成功: {}", item.getLabel());
    }

    @Override
    @Transactional
    @CacheEvict(value = "dictItem", allEntries = true)
    public void updateDictItemStatus(String id, Integer status) {
        DictItemEntity item = dictItemRepository.findById(id)
                .orElseThrow(() -> new BizException("字典项不存在"));

        item.setStatus(status);
        dictItemRepository.save(item);
        log.info("更新字典项状态成功: {}", item.getLabel());
    }

    /**
     * 创建包含子节点的新 DictItemResp 实例（用于 record 类型）
     */
    private DictItemResp createWithChildren(DictItemResp original, List<DictItemResp> children) {
        return new DictItemResp(
                original.id(),
                original.dictId(),
                original.dictType(),
                original.parentId(),
                original.label(),
                original.value(),
                original.sortOrder(),
                original.status(),
                original.remark(),
                children,
                original.createTime(),
                original.updateTime()
        );
    }

    /**
     * 转换为响应 VO
     */
    private DictItemResp toResp(DictItemEntity item, String dictType) {
        String parentId = item.getParent() != null ? item.getParent().getId() : "0";
        return new DictItemResp(
                item.getId(),
                item.getDictId(),
                dictType,
                parentId,
                item.getLabel(),
                item.getValue(),
                item.getSortOrder(),
                item.getStatus(),
                item.getRemark(),
                null, // children 在构建树时填充
                item.getCreateTime(),
                item.getUpdateTime()
        );
    }

    /**
     * 转换为响应 VO（带字典类型）
     */
    private DictItemResp toRespWithDictType(DictItemEntity item, String dictType) {
        String parentId = item.getParent() != null ? item.getParent().getId() : "0";
        return new DictItemResp(
                item.getId(),
                item.getDictId(),
                dictType,
                parentId,
                item.getLabel(),
                item.getValue(),
                item.getSortOrder(),
                item.getStatus(),
                item.getRemark(),
                null,
                item.getCreateTime(),
                item.getUpdateTime()
        );
    }

    /**
     * 检查是否存在循环引用
     */
    private boolean isCircularReference(String newParentId, String currentId, String dictId) {
        if (newParentId == null) {
            return false;
        }

        DictItemEntity currentParent = dictItemRepository.findById(newParentId).orElse(null);
        int maxDepth = 100; // 防止无限循环

        while (currentParent != null && maxDepth-- > 0) {
            if (currentParent.getId().equals(currentId)) {
                return true;
            }
            if (!currentParent.getDictId().equals(dictId)) {
                break;
            }
            currentParent = currentParent.getParent();
        }
        return false;
    }
}
