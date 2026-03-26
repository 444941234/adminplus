package com.adminplus.statemachine.persist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring State Machine 持久化仓库
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@Repository
public interface StateMachineRepository extends JpaRepository<StateMachineEntity, String> {

    /**
     * 根据机器ID查找
     *
     * @param machineId 机器ID
     * @return 实体对象
     */
    Optional<StateMachineEntity> findByMachineId(String machineId);
}
