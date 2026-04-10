package com.adminplus.service.impl;

import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.dto.query.DictQuery;
import com.adminplus.pojo.dto.request.DictCreateRequest;
import com.adminplus.pojo.dto.request.DictUpdateRequest;
import com.adminplus.pojo.dto.response.DictItemResponse;
import com.adminplus.pojo.dto.response.DictResponse;
import com.adminplus.pojo.dto.response.PageResultResponse;
import com.adminplus.pojo.entity.DictEntity;
import com.adminplus.pojo.entity.DictItemEntity;
import com.adminplus.repository.DictItemRepository;
import com.adminplus.repository.DictRepository;
import com.adminplus.utils.EntityHelper;
import com.adminplus.utils.PageUtils;
import com.adminplus.service.DictService;
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
    private final ConversionService conversionService;

    @Override
    @Transactional(readOnly = true)
    public PageResultResponse<DictResponse> getDictList(DictQuery query) {
        Pageable pageable = PageUtils.toPageableDesc(query.getPage(), query.getSize(), "createTime");

        Specification<DictEntity> spec = (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), false));

            String keyword = query.getKeyword();
            if (keyword != null && !keyword.isEmpty()) {
                predicates.add(cb.or(
                        cb.like(root.get("dictType"), "%" + keyword + "%"),
                        cb.like(root.get("dictName"), "%" + keyword + "%")
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        var pageResult = dictRepository.findAll(spec, pageable);
        return PageResultResponse.from(pageResult, e -> conversionService.convert(e, DictResponse.class));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "dict", key = "'id:' + #id")
    public DictResponse getDictById(String id) {
        DictEntity dict = EntityHelper.findByIdOrThrow(dictRepository::findById, id, "字典不存在");
        return conversionService.convert(dict, DictResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "dict", key = "'type:' + #dictType")
    public DictResponse getDictByType(String dictType) {
        DictEntity dict = dictRepository.findByDictType(dictType)
                .orElseThrow(() -> new BizException("字典不存在"));
        return conversionService.convert(dict, DictResponse.class);
    }

    @Override
    @Transactional
    @CacheEvict(value = "dict", allEntries = true)
    public DictResponse createDict(DictCreateRequest request) {
        if (dictRepository.existsByDictType(request.dictType())) {
            throw new BizException("字典类型已存在");
        }

        DictEntity dict = conversionService.convert(request, DictEntity.class);
        dict = dictRepository.save(dict);

        return conversionService.convert(dict, DictResponse.class);
    }

    @Override
    @Transactional
    @CacheEvict(value = "dict", allEntries = true)
    public DictResponse updateDict(String id, DictUpdateRequest request) {
        DictEntity dict = EntityHelper.findByIdOrThrow(dictRepository::findById, id, "字典不存在");

        dict.setDictName(request.dictName());
        if (request.status() != null) {
            dict.setStatus(request.status());
        }
        dict.setRemark(request.remark());

        dict = dictRepository.save(dict);

        return conversionService.convert(dict, DictResponse.class);
    }

    @Override
    @Transactional
    @CacheEvict(value = "dict", allEntries = true)
    public void deleteDict(String id) {
        DictEntity dict = EntityHelper.findByIdOrThrow(dictRepository::findById, id, "字典不存在");

        List<DictItemEntity> items = dictItemRepository.findByDictIdOrderBySortOrderAsc(id);
        if (!items.isEmpty()) {
            throw new BizException("该字典下存在字典项，无法删除");
        }

        dict.setDeleted(true);
        dictRepository.save(dict);
    }

    @Override
    @Transactional
    @CacheEvict(value = "dict", allEntries = true)
    public void updateDictStatus(String id, Integer status) {
        DictEntity dict = EntityHelper.findByIdOrThrow(dictRepository::findById, id, "字典不存在");

        dict.setStatus(status);
        dictRepository.save(dict);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "dict", key = "'items:' + #dictType")
    public List<DictItemResponse> getDictItemsByType(String dictType) {
        DictEntity dict = dictRepository.findByDictType(dictType)
                .orElseThrow(() -> new BizException("字典不存在"));

        return dictItemRepository.findByDictIdAndStatusOrderBySortOrderAsc(dict.getId(), 1).stream()
                .map(item -> toItemVO(item, dict))
                .toList();
    }

    private DictItemResponse toItemVO(DictItemEntity item, DictEntity dict) {
        String parentId = item.getParent() != null ? item.getParent().getId() : "0";
        return new DictItemResponse(
                item.getId(),
                item.getDictId(),
                dict.getDictType(),
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
}