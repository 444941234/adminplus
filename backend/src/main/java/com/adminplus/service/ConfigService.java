package com.adminplus.service;

import com.adminplus.pojo.dto.req.*;
import com.adminplus.pojo.dto.resp.*;
import com.adminplus.pojo.dto.resp.PageResultResp;

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
     * @param page     页码
     * @param size     每页大小
     * @param groupId  配置组ID（可选）
     * @param keyword  关键字（搜索名称或键）
     * @param status   状态（可选）
     * @return 分页结果
     */
    PageResultResp<ConfigResp> getConfigList(Integer page, Integer size, String groupId, String keyword, Integer status);

    /**
     * 根据ID查询配置
     *
     * @param id 配置ID
     * @return 配置详情
     */
    ConfigResp getConfigById(String id);

    /**
     * 根据配置键查询
     *
     * @param key 配置键
     * @return 配置详情
     */
    ConfigResp getConfigByKey(String key);

    /**
     * 根据配置组ID查询配置列表
     *
     * @param groupId 配置组ID
     * @return 配置列表
     */
    List<ConfigResp> getConfigsByGroupId(String groupId);

    /**
     * 创建配置
     *
     * @param req 创建请求
     * @return 创建的配置
     */
    ConfigResp createConfig(ConfigCreateReq req);

    /**
     * 更新配置
     *
     * @param id  配置ID
     * @param req 更新请求
     * @return 更新后的配置
     */
    ConfigResp updateConfig(String id, ConfigUpdateReq req);

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
     * @param req 批量更新请求
     * @return 更新结果
     */
    ConfigImportResultResp batchUpdateConfigs(ConfigBatchUpdateReq req);

    /**
     * 导出配置
     *
     * @param groupIds 配置组ID列表（可选，为空则导出所有）
     * @return 导出数据
     */
    ConfigExportResp exportConfigs(List<String> groupIds);

    /**
     * 导入配置
     *
     * @param req 导入请求
     * @return 导入结果
     */
    ConfigImportResultResp importConfigs(ConfigImportReq req);

    /**
     * 回滚配置到历史版本
     *
     * @param id  配置ID
     * @param req 回滚请求
     * @return 回滚后的配置
     */
    ConfigResp rollbackConfig(String id, ConfigRollbackReq req);

    /**
     * 查询配置历史记录
     *
     * @param id 配置ID
     * @return 历史记录列表
     */
    List<ConfigHistoryResp> getConfigHistory(String id);

    /**
     * 获取配置生效信息
     *
     * @return 生效信息
     */
    ConfigEffectInfoResp getConfigEffectInfo();

    /**
     * 使配置生效（手动生效模式）
     *
     * @param id 配置ID
     */
    void applyConfig(String id);
}
