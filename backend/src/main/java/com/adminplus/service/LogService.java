package com.adminplus.service;

import com.adminplus.pojo.dto.req.LogQueryDTO;
import com.adminplus.pojo.dto.resp.LogPageVO;
import com.adminplus.pojo.dto.resp.PageResultResp;
import com.adminplus.pojo.entity.LogEntity;

import java.util.List;

/**
 * 日志服务接口
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
public interface LogService {

    /**
     * 记录操作日志
     *
     * @param module        操作模块
     * @param operationType 操作类型
     * @param description   操作描述
     */
    void log(String module, Integer operationType, String description);

    /**
     * 记录操作日志（带请求信息）
     *
     * @param module        操作模块
     * @param operationType 操作类型
     * @param description   操作描述
     * @param method        请求方法
     * @param params        请求参数
     * @param ip            请求IP
     */
    void log(String module, Integer operationType, String description,
             String method, String params, String ip);

    /**
     * 记录操作日志（带执行时长）
     *
     * @param module        操作模块
     * @param operationType 操作类型
     * @param description   操作描述
     * @param costTime      执行时长（毫秒）
     */
    void log(String module, Integer operationType, String description, Long costTime);

    /**
     * 记录操作日志（带状态）
     *
     * @param module        操作模块
     * @param operationType 操作类型
     * @param description   操作描述
     * @param status        状态（1=成功，0=失败）
     * @param errorMsg      异常信息
     */
    void log(String module, Integer operationType, String description,
             Integer status, String errorMsg);

    /**
     * 分页查询日志列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResultResp<LogPageVO> findPage(LogQueryDTO query);

    /**
     * 根据ID查询日志详情
     *
     * @param id 日志ID
     * @return 日志详情
     */
    LogPageVO findById(String id);

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
}