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
     *
     * @param dictId 字典ID
     * @return 字典项列表
     */
    List<DictItemResponse> getDictItemsByDictId(String dictId);

    /**
     * 根据字典ID查询字典项树形结构
     *
     * @param dictId 字典ID
     * @return 字典项树形结构
     */
    List<DictItemResponse> getDictItemTreeByDictId(String dictId);

    /**
     * 根据字典类型查询字典项列表（仅启用状态的）
     *
     * @param dictType 字典类型编码
     * @return 启用状态的字典项列表
     */
    List<DictItemResponse> getDictItemsByType(String dictType);

    /**
     * 根据ID查询字典项
     *
     * @param id 字典项ID
     * @return 字典项信息
     * @throws BizException 当字典项不存在时抛出
     */
    DictItemResponse getDictItemById(String id);

    /**
     * 创建字典项
     *
     * @param request 字典项创建请求
     * @return 创建的字典项信息
     * @throws BizException 当父字典项不存在或字典不存在时抛出
     */
    DictItemResponse createDictItem(DictItemCreateRequest request);

    /**
     * 更新字典项
     *
     * @param id      字典项ID
     * @param request 字典项更新请求
     * @return 更新后的字典项信息
     * @throws BizException 当字典项不存在时抛出
     */
    DictItemResponse updateDictItem(String id, DictItemUpdateRequest request);

    /**
     * 删除字典项
     *
     * @param id 字典项ID
     * @throws BizException 当字典项不存在或存在子项时抛出
     */
    void deleteDictItem(String id);

    /**
     * 更新字典项状态
     *
     * @param id     字典项ID
     * @param status 状态值（0:禁用, 1:启用）
     * @throws BizException 当字典项不存在时抛出
     */
    void updateDictItemStatus(String id, Integer status);
}