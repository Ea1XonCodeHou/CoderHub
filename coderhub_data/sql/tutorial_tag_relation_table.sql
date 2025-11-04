-- ================================================
-- 教程标签关系表（Tutorial Tag Relation Table）
-- 说明：教程与标签的多对多关系表，复用现有tag表
-- ================================================

DROP TABLE IF EXISTS `tutorial_tag_relation`;

CREATE TABLE `tutorial_tag_relation` (
    -- 基础信息
    `id` VARCHAR(36) PRIMARY KEY COMMENT '关系ID（UUID）',
    `tutorial_id` VARCHAR(36) NOT NULL COMMENT '教程ID',
    `tag_id` VARCHAR(36) NOT NULL COMMENT '标签ID（复用tag表）',
    
    -- 索引
    INDEX `idx_tutorial_id` (`tutorial_id`),
    INDEX `idx_tag_id` (`tag_id`),
    
    -- 唯一索引（防止重复关联）
    UNIQUE INDEX `idx_unique_relation` (`tutorial_id`, `tag_id`),
    
    -- 外键约束
    FOREIGN KEY (`tutorial_id`) REFERENCES `tutorial`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教程标签关系表';
