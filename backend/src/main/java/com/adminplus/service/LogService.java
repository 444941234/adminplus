package com.adminplus.service;

import com.adminplus.pojo.dto.req.LogEntry;
import com.adminplus.pojo.dto.req.LogQueryReq;
import com.adminplus.pojo.dto.resp.LogPageResp;
import com.adminplus.pojo.dto.resp.LogStatisticsResp;
import com.adminplus.pojo.dto.resp.PageResultResp;

import java.util.List;

/**
 * 日志服务接口
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
public interface LogService {

    /**
     * 记录日志（统一入口）
     *
     * @param entry 日志条目
     */
    void log(LogEntry entry);

    /**
     * 分页查询日志列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResultResp<LogPageResp> findPage(LogQueryReq query);

    /**
     * 根据ID查询日志详情
     *
     * @param id 日志ID
     * @return 日志详情
     */
    LogPageResp findById(String id);

    /**
     * 删除单条日志
     *
     * @param id 日志ID
     */
    void deleteById(String id);

    /**
     * 批量删除日志
     *
     * @param ids 日志ID列表
     */
    void deleteByIds(List<String> ids);

    /**
     * 根据条件删除日志
     *
     * @param query 查询条件
     * @return 删除的记录数
     */
    Integer deleteByCondition(LogQueryReq query);

    /**
     * 清理过期日志
     *
     * @return 清理的记录数
     */
    Integer cleanupExpiredLogs();

    /**
     * 获取日志统计
     *
     * @return 统计数据
     */
    LogStatisticsResp getStatistics();
}