package com.adminplus.repository;

import com.adminplus.pojo.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 通知数据访问层
 *
 * @author AdminPlus
 * @since 2026-04-04
 */
@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, String> {

    /**
     * 查询用户的通知（按创建时间倒序）
     */
    Page<NotificationEntity> findByRecipientIdOrderByCreateTimeDesc(String recipientId, Pageable pageable);

    /**
     * 查询用户的通知（按状态和创建时间倒序）
     */
    Page<NotificationEntity> findByRecipientIdAndStatusOrderByCreateTimeDesc(String recipientId, Integer status, Pageable pageable);

    /**
     * 统计用户未读通知数量
     */
    long countByRecipientIdAndStatus(String recipientId, Integer status);

    /**
     * 批量标记用户通知为已读
     */
    @Modifying
    @Query("UPDATE NotificationEntity n SET n.status = 1, n.updateTime = CURRENT_TIMESTAMP WHERE n.recipientId = :recipientId AND n.status = 0")
    int markAllAsRead(@Param("recipientId") String recipientId);
}
