package com.adminplus.service;

import com.adminplus.pojo.dto.query.ConfigGroupQuery;
import com.adminplus.pojo.dto.request.ConfigGroupCreateRequest;
import com.adminplus.pojo.dto.request.ConfigGroupUpdateRequest;
import com.adminplus.pojo.dto.response.ConfigGroupResponse;
import com.adminplus.pojo.dto.response.PageResultResponse;

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
     * @param query 查询条件
     * @return 分页结果
     */
    PageResultResponse<ConfigGroupResponse> getConfigGroupList(ConfigGroupQuery query);

    /**
     * 根据ID查询配置组
     *
     * @param id 配置组ID
     * @return 配置组详情
     */
    ConfigGroupResponse getConfigGroupById(String id);

    /**
     * 根据编码查询配置组
     *
     * @param code 配置组编码
     * @return 配置组详情
     */
    ConfigGroupResponse getConfigGroupByCode(String code);

    /**
     * 创建配置组
     *
     * @param request 创建请求
     * @return 创建的配置组
     */
    ConfigGroupResponse createConfigGroup(ConfigGroupCreateRequest request);

    /**
     * 更新配置组
     *
     * @param id  配置组ID
     * @param req 更新请求
     * @return 更新后的配置组
     */
    ConfigGroupResponse updateConfigGroup(String id, ConfigGroupUpdateRequest req);

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
    java.util.List<ConfigGroupResponse> getActiveConfigGroups();

    /**
     * 查询所有配置组（包括禁用的）
     *
     * @return 配置组列表
     */
    java.util.List<ConfigGroupResponse> getAllConfigGroups();
}
