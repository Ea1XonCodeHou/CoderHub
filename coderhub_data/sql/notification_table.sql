-- ====================================
-- CoderHub 消息通知表设计
-- ====================================

USE coder_hub;

-- 消息通知表
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `receiver_id` VARCHAR(36) NOT NULL COMMENT '接收者用户ID（UUID）',
  `type` VARCHAR(50) NOT NULL COMMENT '消息类型：COMMUNITY_LIKE(点赞)/COMMUNITY_COMMENT(评论)/COMMUNITY_FOLLOW(关注)/SYSTEM_AUDIT(审核通过)/SYSTEM_WARNING(系统警告)',
  `content` VARCHAR(500) NOT NULL COMMENT '消息内容文案',
  `source_id` VARCHAR(36) COMMENT '来源ID（文章ID等，UUID）',
  `source_type` VARCHAR(20) COMMENT '来源类型：ARTICLE(文章)',
  `trigger_id` VARCHAR(36) COMMENT '触发者用户ID（UUID，社区消息有值，系统消息为NULL）',
  `is_read` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `read_at` DATETIME COMMENT '已读时间',
  PRIMARY KEY (`id`),
  INDEX `idx_receiver_isread` (`receiver_id`, `is_read`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户消息通知表';

-- 查询验证
SELECT * FROM `notification`;

-- ====================================
-- 说明文档
-- ====================================
/*
【字段说明】
1. id: 自增主键
2. receiver_id: 接收者用户ID（关联user表的UUID）
3. type: 消息类型
   - COMMUNITY_LIKE: 点赞通知
   - COMMUNITY_COMMENT: 评论通知
   - COMMUNITY_FOLLOW: 关注通知
   - SYSTEM_AUDIT: 文章审核通过
   - SYSTEM_WARNING: 系统警告
4. content: 消息内容文案
5. source_id: 来源ID（如文章ID）
6. source_type: 来源类型（如ARTICLE）
7. trigger_id: 触发者用户ID（社区消息有值，系统消息为NULL）
8. is_read: 是否已读（0-未读，1-已读）
9. created_at: 创建时间
10. read_at: 已读时间

【索引说明】
- idx_receiver_isread: 联合索引，优化按用户查询未读消息
- idx_created_at: 时间索引，优化按时间排序
*/
