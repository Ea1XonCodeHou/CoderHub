-- ================================================
-- 教程章节表（Tutorial Chapter Table）
-- 说明：存储教程的章节信息，具体的文档和视频通过独立表关联
-- ================================================

DROP TABLE IF EXISTS `tutorial_chapter`;

CREATE TABLE `tutorial_chapter` (
    -- 基础信息
    `id` VARCHAR(36) PRIMARY KEY COMMENT '章节ID（UUID）',
    `tutorial_id` VARCHAR(36) NOT NULL COMMENT '关联教程ID',
    
    -- 章节信息
    `chapter_order` INT NOT NULL COMMENT '章节排序（数字越小越靠前）',
    `chapter_title` VARCHAR(255) NOT NULL COMMENT '章节标题',
    `duration` VARCHAR(50) COMMENT '本章时长（如：45分钟）',
    
    -- 权限控制
    `is_free` TINYINT DEFAULT 0 COMMENT '是否免费试看：0-否 1-是',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0-未发布 1-已发布',
    
    -- 时间戳
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 索引
    INDEX `idx_tutorial_id` (`tutorial_id`),
    INDEX `idx_chapter_order` (`chapter_order`),
    INDEX `idx_status` (`status`),
    
    -- 外键约束
    FOREIGN KEY (`tutorial_id`) REFERENCES `tutorial`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教程章节表';
