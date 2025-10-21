-- ================================================
-- 文章表（Article Table）
-- 说明：存储文章的元数据，Markdown文件存储在OSS
-- ================================================

DROP TABLE IF EXISTS `article`;

CREATE TABLE `article` (
    -- 基础信息
    `id` VARCHAR(36) PRIMARY KEY COMMENT '文章ID（UUID）',
    `user_id` VARCHAR(36) NOT NULL COMMENT '作者ID',
    `title` VARCHAR(255) NOT NULL COMMENT '文章标题',
    `summary` TEXT COMMENT '文章摘要（前200字，用于列表展示）',
    
    -- 内容存储
    `content_url` VARCHAR(500) NOT NULL COMMENT 'Markdown文件OSS地址',
    `cover_image` VARCHAR(500) COMMENT '封面图URL',
    
    -- 分类和标签
    `category_id` VARCHAR(36) COMMENT '分类ID',
    
    -- 统计信息
    `view_count` BIGINT DEFAULT 0 COMMENT '阅读量',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT DEFAULT 0 COMMENT '评论数',
    `collect_count` INT DEFAULT 0 COMMENT '收藏数',
    
    -- 状态管理
    `status` TINYINT DEFAULT 0 COMMENT '状态：0草稿 1已发布 2审核中 3已下架',
    `is_top` TINYINT DEFAULT 0 COMMENT '是否置顶：0否 1是',
    `is_original` TINYINT DEFAULT 1 COMMENT '是否原创：0转载 1原创',
    
    -- 审核信息（管理端需要，暂不使用）
    `audit_status` TINYINT DEFAULT 0 COMMENT '审核状态：0待审核 1通过 2拒绝',
    `audit_reason` VARCHAR(500) COMMENT '审核备注',
    `audit_time` DATETIME COMMENT '审核时间',
    
    -- 时间戳
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `publish_time` DATETIME COMMENT '发布时间',
    
    -- 索引
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_category_id` (`category_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_publish_time` (`publish_time`),
    INDEX `idx_view_count` (`view_count`),
    FULLTEXT INDEX `idx_title_summary` (`title`, `summary`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';