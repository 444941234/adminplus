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
     *
     * @param id 字典ID
     * @return 字典信息
     * @throws BizException 当字典不存在时抛出
     */
    DictResponse getDictById(String id);

    /**
     * 根据字典类型查询
     *
     * @param dictType 字典类型编码
     * @return 字典信息
     * @throws BizException 当字典类型不存在时抛出
     */
    DictResponse getDictByType(String dictType);

    /**
     * 根据字典类型查询字典项
     *
     * @param dictType 字典类型编码
     * @return 字典项列表
     */
    List<DictItemResponse> getDictItemsByType(String dictType);

    /**
     * 创建字典
     *
     * @param request 字典创建请求
     * @return 创建的字典信息
     * @throws BizException 当字典类型编码已存在时抛出
     */
    DictResponse createDict(DictCreateRequest request);

    /**
     * 更新字典
     *
     * @param id      字典ID
     * @param request 字典更新请求
     * @return 更新后的字典信息
     * @throws BizException 当字典不存在时抛出
     */
    DictResponse updateDict(String id, DictUpdateRequest request);

    /**
     * 删除字典
     *
     * @param id 字典ID
     * @throws BizException 当字典不存在或存在字典项时抛出
     */
    void deleteDict(String id);

    /**
     * 更新字典状态
     *
     * @param id     字典ID
     * @param status 状态值（0:禁用, 1:启用）
     * @throws BizException 当字典不存在时抛出
     */
    void updateDictStatus(String id, Integer status);
}