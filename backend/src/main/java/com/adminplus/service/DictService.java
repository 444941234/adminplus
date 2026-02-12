package com.adminplus.service;

import com.adminplus.pojo.dto.req.DictCreateReq;
import com.adminplus.pojo.dto.req.DictUpdateReq;
import com.adminplus.pojo.dto.resp.DictItemResp;
import com.adminplus.pojo.dto.resp.DictResp;
import com.adminplus.pojo.dto.resp.PageResultResp;

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
     */
    PageResultResp<DictResp> getDictList(Integer page, Integer size, String keyword);

    /**
     * 根据ID查询字典
     */
    DictResp getDictById(String id);

    /**
     * 根据字典类型查询
     */
    DictResp getDictByType(String dictType);

    /**
     * 根据字典类型查询字典项
     */
    List<DictItemResp> getDictItemsByType(String dictType);

    /**
     * 创建字典
     */
    DictResp createDict(DictCreateReq req);

    /**
     * 更新字典
     */
    DictResp updateDict(String id, DictUpdateReq req);

    /**
     * 删除字典
     */
    void deleteDict(String id);

    /**
     * 更新字典状态
     */
    void updateDictStatus(String id, Integer status);
}