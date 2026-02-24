package com.adminplus.pojo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * 操作日志实体
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "sys_log",
       indexes = {
           @Index(name = "idx_log_user_id", columnList = "user_id"),
           @Index(name = "idx_log_create_time", columnList = "create_time"),
           @Index(name = "idx_log_operation_type", columnList = "operation_type"),
           @Index(name = "idx_log_deleted", columnList = "deleted")
       })
@SQLDelete(sql = "UPDATE sys_log SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class LogEntity extends BaseEntity {

    /**
     * 操作人ID
     */
    @Column(name = "user_id")
    private String userId;

    /**
     * 操作人用户名
     */
    @Column(name = "username", length = 50)
    private String username;

    /**
     * 操作模块
     */
    @Column(name = "module", length = 50)
    private String module;

    /**
     * 操作类型（1=查询，2=新增，3=修改，4=删除，5=导出，6=导入，7=其他）
     */
    @Column(name = "operation_type")
    private Integer operationType;

    /**
     * 操作描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 请求方法
     */
    @Column(name = "method", length = 200)
    private String method;

    /**
     * 请求参数
     */
    @Column(name = "params", columnDefinition = "text")
    private String params;

    /**
     * 请求IP
     */
    @Column(name = "ip", length = 50)
    private String ip;

    /**
     * 请求地点
     */
    @Column(name = "location", length = 100)
    private String location;

    /**
     * 浏览器类型
     */
    @Column(name = "browser", length = 50)
    private String browser;

    /**
     * 操作系统
     */
    @Column(name = "os", length = 50)
    private String os;

    /**
     * 执行时长（毫秒）
     */
    @Column(name = "cost_time")
    private Long costTime;

    /**
     * 状态（1=成功，0=失败）
     */
    @Column(name = "status")
    private Integer status;

    /**
     * 异常信息
     */
    @Column(name = "error_msg", columnDefinition = "text")
    private String errorMsg;
}