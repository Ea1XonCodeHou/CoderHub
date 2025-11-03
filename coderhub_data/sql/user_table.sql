-- ====================================
-- CoderHub 用户表设计
-- ====================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS coder_hub DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE coder_hub;

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` VARCHAR(36) PRIMARY KEY COMMENT '用户ID（UUID）',
  `account` VARCHAR(8) UNIQUE NOT NULL COMMENT '账户编号（系统分配的8位随机数，用于登录）',
  `username` VARCHAR(50) UNIQUE COMMENT '用户名（用户自定义，用于登录，可为空）',
  `password` VARCHAR(100) NOT NULL COMMENT '密码（MD5加密）',
  `phone` VARCHAR(11) UNIQUE COMMENT '手机号（用于登录）',
  `email` VARCHAR(100) UNIQUE COMMENT '邮箱（用于登录）',
  `avatar` VARCHAR(255) DEFAULT 'https://coderhub-avatar.oss-cn-hangzhou.aliyuncs.com/default.png' COMMENT '头像URL',
  `user_level` INT(20) DEFAULT 'NORMAL' COMMENT '会员等级：NORMAL-普通 VIP-会员 SVIP-超级会员',
  `status` INT DEFAULT 1 COMMENT '账号状态：1-正常 0-禁用',
  `role` VARCHAR(20) DEFAULT 'USER' COMMENT '角色：USER-普通用户 ADMIN-管理员',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  -- 索引优化
  INDEX `idx_account` (`account`),
  INDEX `idx_username` (`username`),
  INDEX `idx_phone` (`phone`),
  INDEX `idx_email` (`email`),
  INDEX `idx_user_level` (`user_level`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 插入测试数据
INSERT INTO `user` (`id`, `account`, `username`, `password`, `phone`, `email`, `user_level`, `role`) VALUES
-- 管理员账号（密码：admin123）
('550e8400-e29b-41d4-a716-446655440000', '10000001', 'admin', '0192023a7bbd73250516f069df18b500', '13800138000', 'admin@coderhub.com', 'SVIP', 'ADMIN'),

-- 普通用户（密码：123456）
('550e8400-e29b-41d4-a716-446655440001', '10000002', 'tech_newbie', 'e10adc3949ba59abbe56e057f20f883e', '13800138001', 'user01@example.com', 'NORMAL', 'USER'),

-- VIP用户（密码：123456，未设置username）
('550e8400-e29b-41d4-a716-446655440002', '10000003', NULL, 'e10adc3949ba59abbe56e057f20f883e', '13800138002', 'user02@example.com', 'VIP', 'USER'),

-- SVIP用户（密码：123456）
('550e8400-e29b-41d4-a716-446655440003', '10000004', 'architect', 'e10adc3949ba59abbe56e057f20f883e', '13800138003', 'user03@example.com', 'SVIP', 'USER'),

-- 测试用户（密码：123456，仅设置手机号注册）
('550e8400-e29b-41d4-a716-446655440004', '10000005', NULL, 'e10adc3949ba59abbe56e057f20f883e', '13800138004', NULL, 'NORMAL', 'USER');

-- 查询验证
SELECT * FROM `user`;

-- ====================================
-- 说明文档
-- ====================================
/*
【字段说明】
1. id: 使用UUID，保证全局唯一，便于分布式部署
2. account: 系统自动分配的8位账户编号，必填，唯一，可用于登录
3. username: 用户自定义用户名，选填，唯一，可用于登录
4. password: 密码（MD5加密），必填
5. phone: 手机号，选填，唯一，可用于登录
6. email: 邮箱，选填，唯一，可用于登录
7. user_level: 会员等级 (NORMAL/VIP/SVIP)
8. status: 账号状态 (1-正常 0-禁用)
9. role: 用户角色 (USER/ADMIN)

【登录方式支持】
- 账户编号 + 密码 (account)
- 用户名 + 密码 (username)
- 手机号 + 密码 (phone)
- 邮箱 + 密码 (email)

【注册流程】
1. 用户注册时必填：密码 + (手机号 或 邮箱)
2. 系统自动生成：UUID作为id，8位随机数作为account
3. 用户后期可以设置：username（可选）

【测试账号】
| 登录方式 | 账号 | 密码 | 说明 |
|---------|------|------|------|
| 账户编号 | 10000001 | admin123 | 管理员 |
| 用户名   | admin | admin123 | 管理员 |
| 手机号   | 13800138000 | admin123 | 管理员 |
| 邮箱     | admin@coderhub.com | admin123 | 管理员 |
| 账户编号 | 10000002 | 123456 | 普通用户 |
| 手机号   | 13800138004 | 123456 | 仅手机号注册的用户 |
*/

