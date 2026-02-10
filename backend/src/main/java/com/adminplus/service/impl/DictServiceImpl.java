package com.adminplus.service.impl;

import com.adminplus.constants.OperationType;
import com.adminplus.dto.DictCreateReq;
import com.adminplus.dto.DictUpdateReq;
import com.adminplus.entity.DictEntity;
import com.adminplus.exception.BizException;
import com.adminplus.repository.DictItemRepository;
import com.adminplus.repository.DictRepository;
import com.adminplus.service.DictService;
import com.adminplus.service.LogService;
import com.adminplus.vo.DictItemVO;
import com.adminplus.vo.DictVO;
import com.adminplus.entity.DictItemEntity;
import com.adminplus.vo.PageResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

/**
 * 字典服务实现
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictServiceImpl implements DictService {

    private final DictRepository dictRepository;
    private final DictItemRepository dictItemRepository;
    private final LogService logService;

    @Override
    @Transactional(readOnly = true)
    // @Cacheable(value = "dict", key = "'list:' + #page + ':' + #size + ':' + (#keyword != null ? #keyword : '')")
    public PageResultVO<DictVO> getDictList(Integer page, Integer size, String keyword) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createTime").descending());

        Specification<DictEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), false));

            if (keyword != null && !keyword.isEmpty()) {
                predicates.add(cb.or(
                        cb.like(root.get("dictType"), "%" + keyword + "%"),
                        cb.like(root.get("dictName"), "%" + keyword + "%")
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        var pageResult = dictRepository.findAll(spec, pageable);
        var records = pageResult.getContent().stream()
                .map(this::toVO)
                .toList();

        return new PageResultVO<>(
                records,
                pageResult.getTotalElements(),
                pageResult.getNumber() + 1,
                pageResult.getSize()
        );
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "dict", key = "'id:' + #id")
    public DictVO getDictById(String id) {
        DictEntity dict = dictRepository.findById(id)
                .orElseThrow(() -> new BizException("字典不存在"));
        return toVO(dict);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "dict", key = "'type:' + #dictType")
    public DictVO getDictByType(String dictType) {
        DictEntity dict = dictRepository.findByDictType(dictType)
                .orElseThrow(() -> new BizException("字典不存在"));
        return toVO(dict);
    }

    @Override
    @Transactional
    @CacheEvict(value = "dict", allEntries = true)
    public DictVO createDict(DictCreateReq req) {
        if (dictRepository.existsByDictType(req.dictType())) {
            throw new BizException("字典类型已存在");
        }

        DictEntity dict = new DictEntity();
        dict.setDictType(req.dictType());
        dict.setDictName(req.dictName());
        dict.setRemark(req.remark());

        dict = dictRepository.save(dict);
        log.info("创建字典成功: {}", dict.getDictType());

        // 记录审计日志
        logService.log("字典管理", OperationType.CREATE, "创建字典: " + dict.getDictName() + " (" + dict.getDictType() + ")");

        return toVO(dict);
    }

    @Override
    @Transactional
    @CacheEvict(value = "dict", allEntries = true)
    public DictVO updateDict(String id, DictUpdateReq req) {
        DictEntity dict = dictRepository.findById(id)
                .orElseThrow(() -> new BizException("字典不存在"));

        dict.setDictName(req.dictName());
        if (req.status() != null) {
            dict.setStatus(req.status());
        }
        dict.setRemark(req.remark());

        dict = dictRepository.save(dict);
        log.info("更新字典成功: {}", dict.getDictType());

        // 记录审计日志
        logService.log("字典管理", OperationType.UPDATE, "更新字典: " + dict.getDictName() + " (" + dict.getDictType() + ")");

        return toVO(dict);
    }

    @Override
    @Transactional
    @CacheEvict(value = "dict", allEntries = true)
    public void deleteDict(String id) {
        DictEntity dict = dictRepository.findById(id)
                .orElseThrow(() -> new BizException("字典不存在"));

        dict.setDeleted(true);
        dictRepository.save(dict);
        log.info("删除字典成功: {}", dict.getDictType());

        // 记录审计日志
        logService.log("字典管理", OperationType.DELETE, "删除字典: " + dict.getDictName() + " (" + dict.getDictType() + ")");
    }

    @Override
    @Transactional
    @CacheEvict(value = "dict", allEntries = true)
    public void updateDictStatus(String id, Integer status) {
        DictEntity dict = dictRepository.findById(id)
                .orElseThrow(() -> new BizException("字典不存在"));

        dict.setStatus(status);
        dictRepository.save(dict);
        log.info("更新字典状态成功: {}", dict.getDictType());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "dict", key = "'items:' + #dictType")
    public List<DictItemVO> getDictItemsByType(String dictType) {
        DictEntity dict = dictRepository.findByDictType(dictType)
                .orElseThrow(() -> new BizException("字典不存在"));

        return dictItemRepository.findByDictIdAndStatusOrderBySortOrderAsc(dict.getId(), 1).stream()
                .map(item -> toItemVO(item, dict))
                .toList();
    }

    private DictVO toVO(DictEntity dict) {
        return new DictVO(
                dict.getId(),
                dict.getDictType(),
                dict.getDictName(),
                dict.getStatus(),
                dict.getRemark(),
                dict.getCreateTime(),
                dict.getUpdateTime()
        );
    }

    private DictItemVO toItemVO(DictItemEntity item, DictEntity dict) {
        return new DictItemVO(
                item.getId(),
                item.getDictId(),
                dict.getDictType(),
                item.getParentId(),
                item.getLabel(),
                item.getValue(),
                item.getSortOrder(),
                item.getStatus(),
                item.getRemark(),
                null, // children - 在构建树形结构时填充
                item.getCreateTime(),
                item.getUpdateTime()
        );
    }
}