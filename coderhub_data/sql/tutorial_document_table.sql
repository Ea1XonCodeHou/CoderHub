-- ================================================
-- 教程文档表（Tutorial Document Table）
-- 说明：存储教程章节的文档资源
-- ================================================

DROP TABLE IF EXISTS `tutorial_document`;

CREATE TABLE `document` (
    -- 基础信息
    `id` VARCHAR(36) PRIMARY KEY COMMENT '文档ID（UUID）',
    `chapter_id` VARCHAR(36) NOT NULL COMMENT '关联章节ID',
    
    -- 文档信息
    `document_title` VARCHAR(255) NOT NULL COMMENT '文档标题',
    `document_url` VARCHAR(500) NOT NULL COMMENT '文档URL（OSS地址或外链）',
    `document_type` VARCHAR(20) DEFAULT 'pdf' COMMENT '文档类型：pdf/md/doc/ppt等',
    `file_size` BIGINT DEFAULT 0 COMMENT '文件大小（字节）',
    
    -- 时间戳
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 索引
    INDEX `idx_chapter_id` (`chapter_id`),
    -- 外键约束
    FOREIGN KEY (`chapter_id`) REFERENCES `tutorial_chapter`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教程文档表';
