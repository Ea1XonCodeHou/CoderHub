CREATE TABLE `project_tech_relation` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `project_id` VARCHAR(36) NOT NULL COMMENT '项目ID',
  `tech_id` BIGINT NOT NULL COMMENT '技术栈ID（关联tech_stack_dict表）',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  
  UNIQUE KEY `uk_project_tech` (`project_id`, `tech_id`),
  INDEX `idx_project_id` (`project_id`),
  INDEX `idx_tech_id` (`tech_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目与技术栈关联表';

