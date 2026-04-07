package com.adminplus.pojo.entity;

import com.adminplus.constants.StorageType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * 文件元数据实体
 *
 * @author AdminPlus
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_file",
       indexes = {
           @Index(name = "idx_file_create_user", columnList = "create_user"),
           @Index(name = "idx_file_directory", columnList = "directory"),
           @Index(name = "idx_file_storage_type", columnList = "storage_type"),
           @Index(name = "idx_file_status", columnList = "status"),
           @Index(name = "idx_file_deleted", columnList = "deleted")
       })
@SQLDelete(sql = "UPDATE sys_file SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class FileEntity extends BaseEntity {

    /**
     * 原始文件名
     */
    @Column(name = "original_name", nullable = false)
    private String originalName;

    /**
     * 文件存储名称（UUID生成）
     */
    @Column(name = "file_name", nullable = false)
    private String fileName;

    /**
     * 文件后缀
     */
    @Column(name = "file_ext")
    private String fileExt;

    /**
     * 文件大小（字节）
     */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /**
     * 文件类型（MIME类型）
     */
    @Column(name = "content_type")
    private String contentType;

    /**
     * 文件访问URL
     */
    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    /**
     * 存储类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "storage_type", nullable = false)
    private StorageType storageType;

    /**
     * 文件目录（如 avatars, files 等）
     */
    @Column(name = "directory")
    private String directory;

    /**
     * 文件状态（1=正常，0=禁用）
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    @PrePersist
    public void prePersist() {
        super.prePersist();
        if (this.status == null) {
            this.status = 1;
        }
    }
}