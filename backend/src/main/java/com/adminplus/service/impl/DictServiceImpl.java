package com.adminplus.service.impl;

import com.adminplus.dto.DictCreateReq;
import com.adminplus.dto.DictUpdateReq;
import com.adminplus.entity.DictEntity;
import com.adminplus.exception.BizException;
import com.adminplus.repository.DictItemRepository;
import com.adminplus.repository.DictRepository;
import com.adminplus.service.DictService;
import com.adminplus.vo.DictItemVO;
import com.adminplus.vo.DictVO;
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

    @Override
    @Transactional(readOnly = true)
    // @Cacheable(value = "dict", key = "'list:' + #page + ':' + #size + ':' + (#keyword != null ? #keyword : '')")
    public List<DictVO> getDictList(Integer page, Integer size, String keyword) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createTime").descending());

        Specification<DictEntity> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), false));

            if (keyword != null && !keyword.isEmpty()) {
                predicates.add(cb.or(
                        cb.like(root.get("dictType"), "%" + keyword + "%"),
                        cb.like(root.get("dictName"), "%" + keyword + "%")
                ));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return dictRepository.findAll(spec, pageable).getContent().stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "dict", key = "'id:' + #id")
    public DictVO getDictById(Long id) {
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
        return toVO(dict);
    }

    @Override
    @Transactional
    @CacheEvict(value = "dict", allEntries = true)
    public DictVO updateDict(Long id, DictUpdateReq req) {
        DictEntity dict = dictRepository.findById(id)
                .orElseThrow(() -> new BizException("字典不存在"));

        dict.setDictName(req.dictName());
        dict.setRemark(req.remark());

        dict = dictRepository.save(dict);
        log.info("更新字典成功: {}", dict.getDictType());
        return toVO(dict);
    }

    @Override
    @Transactional
    @CacheEvict(value = "dict", allEntries = true)
    public void deleteDict(Long id) {
        DictEntity dict = dictRepository.findById(id)
                .orElseThrow(() -> new BizException("字典不存在"));

        dict.setDeleted(true);
        dictRepository.save(dict);
        log.info("删除字典成功: {}", dict.getDictType());
    }

    @Override
    @Transactional
    @CacheEvict(value = "dict", allEntries = true)
    public void updateDictStatus(Long id, Integer status) {
        DictEntity dict = dictRepository.findById(id)
                .orElseThrow(() -> new BizException("字典不存在"));

        dict.setStatus(status);
        dictRepository.save(dict);
        log.info("更新字典状态成功: {}", dict.getDictType());
    }

    @Override
    @Transactional(readOnly = true)
    // @Cacheable(value = "dict", key = "'items:' + #dictType")
    public List<DictItemVO> getDictItemsByType(String dictType) {
        DictEntity dict = dictRepository.findByDictType(dictType)
                .orElseThrow(() -> new BizException("字典不存在"));

        return dictItemRepository.findByDictIdAndStatusOrderBySortOrderAsc(dict.getId(), 1).stream()
                .map(this::toItemVO)
                .toList();
    }

    private DictVO toVO(DictEntity dict) {
        return new DictVO(
                dict.getId(),
                dict.getDictType(),
                dict.getDictName(),
                dict.getRemark(),
                dict.getCreateTime(),
                dict.getUpdateTime()
        );
    }

    private com.adminplus.vo.DictItemVO toItemVO(com.adminplus.entity.DictItemEntity item) {
        // 通过 dictId 查询字典实体来获取 dictType
        com.adminplus.entity.DictEntity dict = dictRepository.findById(item.getDictId())
                .orElseThrow(() -> new BizException("字典不存在"));
        
        return new com.adminplus.vo.DictItemVO(
                item.getId(),
                item.getDictId(),
                dict.getDictType(),
                item.getLabel(),
                item.getValue(),
                item.getSortOrder(),
                item.getStatus(),
                item.getRemark(),
                item.getCreateTime(),
                item.getUpdateTime()
        );
    }
}