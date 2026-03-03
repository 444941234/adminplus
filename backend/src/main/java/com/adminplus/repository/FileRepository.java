package com.adminplus.repository;

import com.adminplus.pojo.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 文件仓储
 *
 * @author AdminPlus
 */
@Repository
public interface FileRepository extends JpaRepository<FileEntity, String> {

    /**
     * 根据文件ID查询
     */
    Optional<FileEntity> findByIdAndDeletedFalse(String id);

    /**
     * 根据文件URL查询
     */
    Optional<FileEntity> findByFileUrlAndDeletedFalse(String fileUrl);

    /**
     * 根据创建用户查询文件列表
     */
    List<FileEntity> findByCreateUserAndDeletedFalseOrderByCreateTimeDesc(String createUser);

    /**
     * 根据目录查询文件列表
     */
    List<FileEntity> findByDirectoryAndDeletedFalseOrderByCreateTimeDesc(String directory);

    /**
     * 统计用户上传文件数量
     */
    long countByCreateUserAndDeletedFalse(String createUser);

    /**
     * 统计用户上传文件总大小
     */
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM FileEntity f WHERE f.createUser = :userId AND f.deleted = false")
    Long sumFileSizeByUser(@Param("userId") String userId);
}