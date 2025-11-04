-- ================================================
-- 教程表（Tutorial Table）
-- 说明：存储教程的元数据，由管理员创建和管理
-- ================================================

DROP TABLE IF EXISTS `tutorial`;

CREATE TABLE `tutorial` (
    -- 基础信息
    `id` VARCHAR(36) PRIMARY KEY COMMENT '教程ID（UUID）',
    `title` VARCHAR(255) NOT NULL COMMENT '教程标题',
    `description` TEXT COMMENT '教程简介',
    `cover_image` VARCHAR(500) COMMENT '封面图URL',
    
    -- 分类和难度
    `category_id` VARCHAR(36) COMMENT '分类ID（复用category表）',
    `difficulty` TINYINT DEFAULT 0 COMMENT '难度：0-入门 1-进阶 2-高级',
    
    -- 讲师信息
    `instructor_name` VARCHAR(100) COMMENT '讲师名称',
    `instructor_avatar` VARCHAR(500) COMMENT '讲师头像URL',
    `instructor_desc` VARCHAR(500) COMMENT '讲师简介',
    
    -- 价格与状态
    `price` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '价格（0表示免费）',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0-草稿 1-已发布 2-下架',
    
    -- 统计信息
    `view_count` BIGINT DEFAULT 0 COMMENT '浏览量',
    `like_count` INT DEFAULT 0 COMMENT '点赞量',
    `recommend_count` INT DEFAULT 0 COMMENT '推荐量',
    `student_count` INT DEFAULT 0 COMMENT '学习人数',
    `rating` DECIMAL(3, 1) DEFAULT 0.0 COMMENT '评分（如4.8）',
    `chapter_count` INT DEFAULT 0 COMMENT '章节数量（冗余字段）',
    
    -- 创建者信息
    `create_user` VARCHAR(36) NOT NULL COMMENT '创建者ID（管理员）',
    
    -- 时间戳
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 索引
    INDEX `idx_category_id` (`category_id`),
    INDEX `idx_difficulty` (`difficulty`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_user` (`create_user`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_student_count` (`student_count`),
    INDEX `idx_rating` (`rating`),
    FULLTEXT INDEX `idx_title_description` (`title`, `description`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教程主表';
