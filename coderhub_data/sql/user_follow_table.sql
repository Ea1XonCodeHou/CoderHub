-- 用户关注关系表
CREATE TABLE IF NOT EXISTS user_follow (
    id CHAR(36) PRIMARY KEY COMMENT '主键ID',
    follower_id CHAR(36) NOT NULL COMMENT '关注者ID（谁关注）',
    followed_id CHAR(36) NOT NULL COMMENT '被关注者ID（被谁关注）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    
    -- 唯一索引：防止重复关注
    UNIQUE KEY uk_follower_followed (follower_id, followed_id),
    
    -- 普通索引：提高查询效率
    KEY idx_follower_id (follower_id),
    KEY idx_followed_id (followed_id),
    
    -- 外键约束（可选，根据实际需求决定是否添加）
    -- FOREIGN KEY (follower_id) REFERENCES user(id) ON DELETE CASCADE,
    -- FOREIGN KEY (followed_id) REFERENCES user(id) ON DELETE CASCADE
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注关系表';

-- 插入测试数据（可选）
-- INSERT INTO user_follow (id, follower_id, followed_id) VALUES
-- (UUID(), 'user1_id', 'user2_id'),
-- (UUID(), 'user1_id', 'user3_id');

