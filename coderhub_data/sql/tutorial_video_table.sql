-- ================================================
-- 教程视频表（Tutorial Video Table）
-- 说明：存储教程章节的视频资源
-- ================================================

DROP TABLE IF EXISTS `tutorial_video`;

CREATE TABLE `tutorial_video` (
    -- 基础信息
    `id` VARCHAR(36) PRIMARY KEY COMMENT '视频ID（UUID）',
    `chapter_id` VARCHAR(36) NOT NULL COMMENT '关联章节ID',
    
    -- 视频信息
    `video_title` VARCHAR(255) NOT NULL COMMENT '视频标题',
    `video_url` VARCHAR(500) NOT NULL COMMENT '视频URL（OSS地址或视频平台外链）',
    `cover_image` VARCHAR(500) COMMENT '视频封面图URL',
    `duration` VARCHAR(50) COMMENT '视频时长（如：45:30）',
    `file_size` BIGINT DEFAULT 0 COMMENT '文件大小（字节）',
    `resolution` VARCHAR(20) DEFAULT '1080p' COMMENT '分辨率：720p/1080p/4k等',
    `sort_order` INT DEFAULT 0 COMMENT '排序序号',
    
    -- 时间戳
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 索引
    INDEX `idx_chapter_id` (`chapter_id`),
    INDEX `idx_sort_order` (`sort_order`),
    
    -- 外键约束
    FOREIGN KEY (`chapter_id`) REFERENCES `tutorial_chapter`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教程视频表';
