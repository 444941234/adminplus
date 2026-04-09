package com.adminplus.service;

import com.adminplus.pojo.dto.request.DictItemCreateRequest;
import com.adminplus.pojo.dto.request.DictItemUpdateRequest;
import com.adminplus.pojo.dto.response.DictItemResponse;

import java.util.List;

/**
 * 字典项服务接口
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
public interface DictItemService {

    /**
     * 根据字典ID查询字典项列表
     */
    List<DictItemResponse> getDictItemsByDictId(String dictId);

    /**
     * 根据字典ID查询字典项树形结构
     */
    List<DictItemResponse> getDictItemTreeByDictId(String dictId);

    /**
     * 根据字典类型查询字典项列表（仅启用状态的）
     */
    List<DictItemResponse> getDictItemsByType(String dictType);

    /**
     * 根据ID查询字典项
     */
    DictItemResponse getDictItemById(String id);

    /**
     * 创建字典项
     */
    DictItemResponse createDictItem(DictItemCreateRequest request);

    /**
     * 更新字典项
     */
    DictItemResponse updateDictItem(String id, DictItemUpdateRequest request);

    /**
     * 删除字典项
     */
    void deleteDictItem(String id);

    /**
     * 更新字典项状态
     */
    void updateDictItemStatus(String id, Integer status);
}