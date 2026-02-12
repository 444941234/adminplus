package com.adminplus.service;

import com.adminplus.pojo.dto.req.DictItemCreateReq;
import com.adminplus.pojo.dto.req.DictItemUpdateReq;
import com.adminplus.pojo.dto.resp.DictItemResp;

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
    List<DictItemResp> getDictItemsByDictId(String dictId);

    /**
     * 根据字典ID查询字典项树形结构
     */
    List<DictItemResp> getDictItemTreeByDictId(String dictId);

    /**
     * 根据字典类型查询字典项列表（仅启用状态的）
     */
    List<DictItemResp> getDictItemsByType(String dictType);

    /**
     * 根据ID查询字典项
     */
    DictItemResp getDictItemById(String id);

    /**
     * 创建字典项
     */
    DictItemResp createDictItem(DictItemCreateReq req);

    /**
     * 更新字典项
     */
    DictItemResp updateDictItem(String id, DictItemUpdateReq req);

    /**
     * 删除字典项
     */
    void deleteDictItem(String id);

    /**
     * 更新字典项状态
     */
    void updateDictItemStatus(String id, Integer status);
}