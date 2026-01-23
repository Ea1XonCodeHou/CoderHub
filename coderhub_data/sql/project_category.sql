CREATE TABLE `project_category` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `category_name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `is_active` TINYINT DEFAULT 1 COMMENT '是否启用：0-禁用 1-启用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  UNIQUE KEY `uk_category_name` (`category_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目分类字典表';

-- 插入默认分类数据
INSERT INTO `project_category` (`category_name`) VALUES
('Web应用'),
('移动应用'),
('桌面应用'),
('后端服务'),
('AI/机器学习'),
('数据分析'),
('物联网'),
('区块链'),
('游戏开发'),
('其他');