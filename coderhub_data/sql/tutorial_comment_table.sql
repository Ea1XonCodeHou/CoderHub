-- ================================================
-- 教程讨论区评论表（Tutorial Comment Table）
-- 说明：存储教程的讨论区评论，支持评论回复
-- ================================================

DROP TABLE IF EXISTS `tutorial_comment`;

CREATE TABLE `tutorial_comment` (
    -- 基础信息
    `id` VARCHAR(36) PRIMARY KEY COMMENT '评论ID（UUID）',
    `tutorial_id` VARCHAR(36) NOT NULL COMMENT '关联教程ID',
    `user_id` VARCHAR(36) NOT NULL COMMENT '评论用户ID',
    
    -- 评论内容
    `content` TEXT NOT NULL COMMENT '评论内容',
    `parent_id` VARCHAR(36) DEFAULT NULL COMMENT '父评论ID（支持回复，NULL表示一级评论）',
    
    -- 统计信息
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    
    -- 时间戳
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    -- 索引
    INDEX `idx_tutorial_id` (`tutorial_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_create_time` (`create_time`),
    
    -- 外键约束
    FOREIGN KEY (`tutorial_id`) REFERENCES `tutorial`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`parent_id`) REFERENCES `tutorial_comment`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教程讨论区评论表';
