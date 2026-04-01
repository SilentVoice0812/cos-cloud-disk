package com.baoge.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cos_file")
public class CosFile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 存储路径（COS中的key）
     */
    private String filePath;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * MIME类型
     */
    private String mimeType;
    
    /**
     * 文件MD5
     */
    private String md5;
    
    /**
     * 文件类型：file-文件，folder-文件夹
     */
    private String fileType;
    
    /**
     * 父目录ID，根目录为0
     */
    private Long parentId;
    
    /**
     * COS桶名
     */
    private String bucketName;
    
    /**
     * COS访问URL
     */
    private String cosUrl;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    private Integer deleted;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (deleted == null) {
            deleted = 0;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
