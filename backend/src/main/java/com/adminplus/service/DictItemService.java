package com.adminplus.service;

import com.adminplus.dto.DictItemCreateReq;
import com.adminplus.dto.DictItemUpdateReq;
import com.adminplus.vo.DictItemVO;

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
    List<DictItemVO> getDictItemsByDictId(String dictId);

    /**
     * 根据字典ID查询字典项树形结构
     */
    List<DictItemVO> getDictItemTreeByDictId(String dictId);

    /**
     * 根据字典类型查询字典项列表（仅启用状态的）
     */
    List<DictItemVO> getDictItemsByType(String dictType);

    /**
     * 根据ID查询字典项
     */
    DictItemVO getDictItemById(String id);

    /**
     * 创建字典项
     */
    DictItemVO createDictItem(DictItemCreateReq req);

    /**
     * 更新字典项
     */
    DictItemVO updateDictItem(String id, DictItemUpdateReq req);

    /**
     * 删除字典项
     */
    void deleteDictItem(String id);

    /**
     * 更新字典项状态
     */
    void updateDictItemStatus(String id, Integer status);
}