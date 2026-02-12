package com.adminplus.service.impl;

import com.adminplus.exception.BizException;
import com.adminplus.pojo.dto.req.DictItemCreateReq;
import com.adminplus.pojo.dto.req.DictItemUpdateReq;
import com.adminplus.pojo.dto.resp.DictItemResp;
import com.adminplus.pojo.entity.DictEntity;
import com.adminplus.pojo.entity.DictItemEntity;
import com.adminplus.repository.DictItemRepository;
import com.adminplus.repository.DictRepository;
import com.adminplus.service.DictItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        return dictItemRepository.findByDictIdOrderBySortOrderAsc(dictId).stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "dictItem", key = "'tree:dictId:' + #dictId")
    public List<DictItemResp> getDictItemTreeByDictId(String dictId) {
        List<DictItemEntity> items = dictItemRepository.findByDictIdOrderBySortOrderAsc(dictId);
        return buildTree(items, null);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "dictItem", key = "'type:' + #dictType")
    public List<DictItemResp> getDictItemsByType(String dictType) {
        DictEntity dict = dictRepository.findByDictType(dictType)
                .orElseThrow(() -> new BizException("字典不存在"));

        return dictItemRepository.findByDictIdAndStatusOrderBySortOrderAsc(dict.getId(), 1).stream()
                .map(item -> toVOWithDictType(item, dict.getDictType()))
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

        return toVOWithDictType(item, dict.getDictType());
    }

    @Override
    @Transactional
    @CacheEvict(value = "dictItem", allEntries = true)
    public DictItemResp createDictItem(DictItemCreateReq req) {
        DictEntity dict = dictRepository.findById(req.dictId())
                .orElseThrow(() -> new BizException("字典不存在"));

        // 验证父节点是否存在且属于同一字典
        if (req.parentId() != null) {
            DictItemEntity parent = dictItemRepository.findById(req.parentId())
                    .orElseThrow(() -> new BizException("父节点不存在"));
            if (!parent.getDictId().equals(req.dictId())) {
                throw new BizException("父节点不属于当前字典");
            }
        }

        DictItemEntity item = new DictItemEntity();
        item.setDictId(req.dictId());
        item.setParentId(req.parentId());
        item.setLabel(req.label());
        item.setValue(req.value());
        item.setSortOrder(req.sortOrder() != null ? req.sortOrder() : 0);
        item.setStatus(req.status() != null ? req.status() : 1);
        item.setRemark(req.remark());

        item = dictItemRepository.save(item);
        log.info("创建字典项成功: {} - {}", dict.getDictType(), item.getLabel());
        return toVOWithDictType(item, dict.getDictType());
    }

    @Override
    @Transactional
    @CacheEvict(value = "dictItem", allEntries = true)
    public DictItemResp updateDictItem(String id, DictItemUpdateReq req) {
        DictItemEntity item = dictItemRepository.findById(id)
                .orElseThrow(() -> new BizException("字典项不存在"));

        DictEntity dict = dictRepository.findById(item.getDictId())
                .orElseThrow(() -> new BizException("字典不存在"));

        // 验证父节点是否存在且属于同一字典
        if (req.parentId() != null && !req.parentId().equals(item.getId())) {
            DictItemEntity parent = dictItemRepository.findById(req.parentId())
                    .orElseThrow(() -> new BizException("父节点不存在"));
            if (!parent.getDictId().equals(item.getDictId())) {
                throw new BizException("父节点不属于当前字典");
            }
            // 防止循环引用
            if (isCircularReference(req.parentId(), id, item.getDictId())) {
                throw new BizException("不能将父节点设置为自己的子节点");
            }
        }

        item.setParentId(req.parentId());
        item.setLabel(req.label());
        item.setValue(req.value());
        item.setSortOrder(req.sortOrder() != null ? req.sortOrder() : item.getSortOrder());
        item.setStatus(req.status() != null ? req.status() : item.getStatus());
        item.setRemark(req.remark());

        item = dictItemRepository.save(item);
        log.info("更新字典项成功: {} - {}", dict.getDictType(), item.getLabel());
        return toVOWithDictType(item, dict.getDictType());
    }

    @Override
    @Transactional
    @CacheEvict(value = "dictItem", allEntries = true)
    public void deleteDictItem(String id) {
        DictItemEntity item = dictItemRepository.findById(id)
                .orElseThrow(() -> new BizException("字典项不存在"));

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

    private DictItemResp toVO(DictItemEntity item) {
        return new DictItemResp(
                item.getId(),
                item.getDictId(),
                null,
                item.getParentId(),
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

    private DictItemResp toVOWithDictType(DictItemEntity item, String dictType) {
        return new DictItemResp(
                item.getId(),
                item.getDictId(),
                dictType,
                item.getParentId(),
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
     * 构建树形结构
     */
    private List<DictItemResp> buildTree(List<DictItemEntity> items, String parentId) {
        Map<String, List<DictItemEntity>> childrenMap = items.stream()
                .filter(item -> (parentId == null && item.getParentId() == null) ||
                                (parentId != null && parentId.equals(item.getParentId())))
                .collect(Collectors.groupingBy(item -> item.getParentId() == null ? "0" : item.getParentId()));

        List<DictItemResp> result = new ArrayList<>();
        for (DictItemEntity item : items) {
            if ((parentId == null && item.getParentId() == null) ||
                (parentId != null && parentId.equals(item.getParentId()))) {
                continue;
            }
        }

        // 获取根节点
        List<DictItemEntity> roots = items.stream()
                .filter(item -> item.getParentId() == null)
                .sorted((a, b) -> a.getSortOrder().compareTo(b.getSortOrder()))
                .toList();

        for (DictItemEntity root : roots) {
            result.add(buildTreeNode(root, items));
        }

        return result;
    }

    /**
     * 构建树节点（递归）
     */
    private DictItemResp buildTreeNode(DictItemEntity parent, List<DictItemEntity> allItems) {
        List<DictItemEntity> children = allItems.stream()
                .filter(item -> parent.getId().equals(item.getParentId()))
                .sorted((a, b) -> a.getSortOrder().compareTo(b.getSortOrder()))
                .toList();

        List<DictItemResp> childVOs = children.stream()
                .map(child -> buildTreeNode(child, allItems))
                .toList();

        return new DictItemResp(
                parent.getId(),
                parent.getDictId(),
                null,
                parent.getParentId(),
                parent.getLabel(),
                parent.getValue(),
                parent.getSortOrder(),
                parent.getStatus(),
                parent.getRemark(),
                childVOs.isEmpty() ? null : childVOs,
                parent.getCreateTime(),
                parent.getUpdateTime()
        );
    }

    /**
     * 检查是否存在循环引用
     */
    private boolean isCircularReference(String newParentId, String currentId, String dictId) {
        if (newParentId == null) {
            return false;
        }

        String currentParentId = newParentId;
        int maxDepth = 100; // 防止无限循环
        while (currentParentId != null && maxDepth-- > 0) {
            if (currentParentId.equals(currentId)) {
                return true;
            }
            DictItemEntity parent = dictItemRepository.findById(currentParentId).orElse(null);
            if (parent == null || !parent.getDictId().equals(dictId)) {
                break;
            }
            currentParentId = parent.getParentId();
        }
        return false;
    }
}