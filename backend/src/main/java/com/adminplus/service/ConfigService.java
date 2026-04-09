package com.adminplus.service;

import com.adminplus.pojo.dto.query.ConfigQuery;
import com.adminplus.pojo.dto.request.*;
import com.adminplus.pojo.dto.response.*;
import com.adminplus.pojo.dto.response.PageResultResponse;

import java.util.List;

/**
 * 配置服务接口
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public interface ConfigService {

    /**
     * 分页查询配置列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResultResponse<ConfigResponse> getConfigList(ConfigQuery query);

    /**
     * 根据ID查询配置
     *
     * @param id 配置ID
     * @return 配置详情
     */
    ConfigResponse getConfigById(String id);

    /**
     * 根据配置键查询
     *
     * @param key 配置键
     * @return 配置详情
     */
    ConfigResponse getConfigByKey(String key);

    /**
     * 根据配置组ID查询配置列表
     *
     * @param groupId 配置组ID
     * @return 配置列表
     */
    List<ConfigResponse> getConfigsByGroupId(String groupId);

    /**
     * 根据配置组编码查询配置列表
     *
     * @param groupCode 配置组编码
     * @return 配置列表
     */
    List<ConfigResponse> getConfigsByGroupCode(String groupCode);

    /**
     * 创建配置
     *
     * @param request 创建请求
     * @return 创建的配置
     */
    ConfigResponse createConfig(ConfigCreateRequest request);

    /**
     * 更新配置
     *
     * @param id  配置ID
     * @param request 更新请求
     * @return 更新后的配置
     */
    ConfigResponse updateConfig(String id, ConfigUpdateRequest request);

    /**
     * 删除配置
     *
     * @param id 配置ID
     */
    void deleteConfig(String id);

    /**
     * 更新配置状态
     *
     * @param id     配置ID
     * @param status 状态（1 启用 / 0 禁用）
     */
    void updateConfigStatus(String id, Integer status);

    /**
     * 批量更新配置值
     *
     * @param request 批量更新请求
     * @return 更新结果
     */
    ConfigImportResultResponse batchUpdateConfigs(ConfigBatchUpdateRequest request);

    /**
     * 导出配置
     *
     * @param groupIds 配置组ID列表（可选，为空则导出所有）
     * @return 导出数据
     */
    ConfigExportResponse exportConfigs(List<String> groupIds);

    /**
     * 导入配置
     *
     * @param request 导入请求
     * @return 导入结果
     */
    ConfigImportResultResponse importConfigs(ConfigImportRequest request);

    /**
     * 回滚配置到历史版本
     *
     * @param id  配置ID
     * @param request 回滚请求
     * @return 回滚后的配置
     */
    ConfigResponse rollbackConfig(String id, ConfigRollbackRequest request);

    /**
     * 查询配置历史记录
     *
     * @param id 配置ID
     * @return 历史记录列表
     */
    List<ConfigHistoryResponse> getConfigHistory(String id);

    /**
     * 获取配置生效信息
     *
     * @return 生效信息
     */
    ConfigEffectInfoResponse getConfigEffectInfo();

    /**
     * 使配置生效（手动生效模式）
     *
     * @param id 配置ID
     */
    void applyConfig(String id);

    /**
     * 刷新配置缓存
     */
    void refreshConfigCache();
}
