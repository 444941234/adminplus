package com.adminplus.service;

import com.adminplus.pojo.dto.query.DictQuery;
import com.adminplus.pojo.dto.request.DictCreateRequest;
import com.adminplus.pojo.dto.request.DictUpdateRequest;
import com.adminplus.pojo.dto.response.DictItemResponse;
import com.adminplus.pojo.dto.response.DictResponse;
import com.adminplus.pojo.dto.response.PageResultResponse;

import java.util.List;

/**
 * 字典服务接口
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
public interface DictService {

    /**
     * 分页查询字典列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResultResponse<DictResponse> getDictList(DictQuery query);

    /**
     * 根据ID查询字典
     */
    DictResponse getDictById(String id);

    /**
     * 根据字典类型查询
     */
    DictResponse getDictByType(String dictType);

    /**
     * 根据字典类型查询字典项
     */
    List<DictItemResponse> getDictItemsByType(String dictType);

    /**
     * 创建字典
     */
    DictResponse createDict(DictCreateRequest request);

    /**
     * 更新字典
     */
    DictResponse updateDict(String id, DictUpdateRequest request);

    /**
     * 删除字典
     */
    void deleteDict(String id);

    /**
     * 更新字典状态
     */
    void updateDictStatus(String id, Integer status);
}