package com.adminplus.statemachine.persist;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

/**
 * Spring State Machine 持久化实体
 * <p>
 * 用于序列化和存储状态机上下文
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@Data
@Entity
@Table(name = "spring_state_machine_context",
       indexes = {
           @Index(name = "idx_state_machine_update", columnList = "update_time")
       })
public class StateMachineEntity {

    /**
     * 机器ID（通常使用流程实例ID）
     */
    @Id
    @Column(name = "machine_id", length = 100)
    private String machineId;

    /**
     * 当前状态
     */
    @Column(name = "state", nullable = false, length = 50)
    private String state;

    /**
     * 扩展状态（JSON格式）
     */
    @Column(name = "extended_state", columnDefinition = "jsonb")
    private String extendedState;

    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false, updatable = false)
    private Instant createTime = Instant.now();

    /**
     * 更新时间
     */
    @Column(name = "update_time", nullable = false)
    private Instant updateTime = Instant.now();

    @PreUpdate
    protected void onUpdate() {
        this.updateTime = Instant.now();
    }
}
