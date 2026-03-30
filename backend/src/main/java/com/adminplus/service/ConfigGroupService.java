package com.adminplus.service;

import com.adminplus.pojo.dto.req.ConfigGroupCreateReq;
import com.adminplus.pojo.dto.req.ConfigGroupUpdateReq;
import com.adminplus.pojo.dto.resp.ConfigGroupResp;
import com.adminplus.pojo.dto.resp.PageResultResp;

/**
 * 配置组服务接口
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public interface ConfigGroupService {

    /**
     * 分页查询配置组列表
     *
     * @param page    页码
     * @param size    每页大小
     * @param keyword 关键字（搜索名称或编码）
     * @return 分页结果
     */
    PageResultResp<ConfigGroupResp> getConfigGroupList(Integer page, Integer size, String keyword);

    /**
     * 根据ID查询配置组
     *
     * @param id 配置组ID
     * @return 配置组详情
     */
    ConfigGroupResp getConfigGroupById(String id);

    /**
     * 根据编码查询配置组
     *
     * @param code 配置组编码
     * @return 配置组详情
     */
    ConfigGroupResp getConfigGroupByCode(String code);

    /**
     * 创建配置组
     *
     * @param req 创建请求
     * @return 创建的配置组
     */
    ConfigGroupResp createConfigGroup(ConfigGroupCreateReq req);

    /**
     * 更新配置组
     *
     * @param id  配置组ID
     * @param req 更新请求
     * @return 更新后的配置组
     */
    ConfigGroupResp updateConfigGroup(String id, ConfigGroupUpdateReq req);

    /**
     * 删除配置组
     *
     * @param id 配置组ID
     */
    void deleteConfigGroup(String id);

    /**
     * 更新配置组状态
     *
     * @param id     配置组ID
     * @param status 状态（1 启用 / 0 禁用）
     */
    void updateConfigGroupStatus(String id, Integer status);

    /**
     * 查询所有启用的配置组（用于下拉选择）
     *
     * @return 配置组列表
     */
    java.util.List<ConfigGroupResp> getActiveConfigGroups();
}
