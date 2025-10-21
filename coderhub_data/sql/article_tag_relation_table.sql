-- ================================================
-- 文章标签关联表（Article Tag Relation Table）
-- 说明：多对多关系的中间表，关联文章和标签
-- ================================================

DROP TABLE IF EXISTS `article_tag_relation`;

CREATE TABLE `article_tag_relation` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '关联ID（UUID）',
    `article_id` VARCHAR(36) NOT NULL COMMENT '文章ID',
    `tag_id` VARCHAR(36) NOT NULL COMMENT '标签ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    -- 唯一索引：防止重复关联
    UNIQUE KEY `uk_article_tag` (`article_id`, `tag_id`),
    -- 普通索引：加速标签查询
    INDEX `idx_tag_id` (`tag_id`),
    INDEX `idx_article_id` (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章标签关联表';


