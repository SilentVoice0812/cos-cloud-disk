-- 创建数据库
CREATE DATABASE IF NOT EXISTS cloud DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE cloud;

-- 创建文件表
CREATE TABLE IF NOT EXISTS cos_file (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_path VARCHAR(500) DEFAULT NULL COMMENT 'COS存储路径',
    file_size BIGINT DEFAULT 0 COMMENT '文件大小（字节）',
    mime_type VARCHAR(100) DEFAULT NULL COMMENT 'MIME类型',
    file_type VARCHAR(20) NOT NULL DEFAULT 'file' COMMENT '文件类型：file-文件，folder-文件夹',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父目录ID，根目录为0',
    bucket_name VARCHAR(100) DEFAULT NULL COMMENT 'COS桶名',
    cos_url VARCHAR(500) DEFAULT NULL COMMENT 'COS访问URL',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_parent_id (parent_id),
    INDEX idx_file_name (file_name),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='云盘文件表';
