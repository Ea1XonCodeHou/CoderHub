-- ================================================
-- 教程用户行为表（Tutorial User Action Table）
-- 说明：记录用户对教程的行为（点赞/推荐/收藏）
-- ================================================

DROP TABLE IF EXISTS `tutorial_user_action`;

CREATE TABLE `tutorial_user_action` (
    -- 基础信息
    `id` VARCHAR(36) PRIMARY KEY COMMENT '行为ID（UUID）',
    `tutorial_id` VARCHAR(36) NOT NULL COMMENT '关联教程ID',
    `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID',
    
    -- 行为类型
    `action_type` VARCHAR(20) NOT NULL COMMENT '行为类型：like-点赞 / recommend-推荐 / collect-收藏',
    
    -- 时间戳
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    -- 索引
    INDEX `idx_tutorial_id` (`tutorial_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_action_type` (`action_type`),
    
    -- 唯一索引（防止重复行为）
    UNIQUE INDEX `idx_unique_action` (`tutorial_id`, `user_id`, `action_type`),
    
    -- 外键约束
    FOREIGN KEY (`tutorial_id`) REFERENCES `tutorial`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教程用户行为表';
