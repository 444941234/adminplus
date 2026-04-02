package com.adminplus.repository;

import com.adminplus.pojo.entity.FormTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 表单模板数据访问接口
 *
 * @author AdminPlus
 * @since 2026-04-02
 */
@Repository
public interface FormTemplateRepository extends JpaRepository<FormTemplateEntity, String>,
        JpaSpecificationExecutor<FormTemplateEntity> {

    /**
     * 根据表单标识查询
     */
    Optional<FormTemplateEntity> findByTemplateCode(String templateCode);

    /**
     * 检查表单标识是否存在
     */
    boolean existsByTemplateCode(String templateCode);

    /**
     * 查询所有启用的表单模板
     */
    List<FormTemplateEntity> findByStatusOrderByCreateTimeDesc(Integer status);

    /**
     * 根据分类查询表单模板
     */
    List<FormTemplateEntity> findByCategoryAndStatusOrderByCreateTimeDesc(String category, Integer status);
}
