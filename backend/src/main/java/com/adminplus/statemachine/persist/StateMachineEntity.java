package com.adminplus.statemachine.persist;

import com.adminplus.pojo.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "spring_state_machine_context",
       indexes = {
           @Index(name = "idx_state_machine_update", columnList = "update_time")
       })
public class StateMachineEntity extends BaseEntity {

    /**
     * 机器ID（通常使用流程实例ID）
     */
    @Column(name = "machine_id", length = 100, unique = true, nullable = false)
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
}
