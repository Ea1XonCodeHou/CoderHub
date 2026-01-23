CREATE TABLE `project` (
  -- 基础字段
  `id` VARCHAR(36) PRIMARY KEY COMMENT '项目ID (UUID)',
  `user_id` VARCHAR(36) NOT NULL COMMENT '创建者ID',
  `project_name` VARCHAR(100) NOT NULL COMMENT '项目名称',
  
  -- 项目类型
  `project_type` TINYINT NOT NULL COMMENT '项目类型：1-优秀开源项目(官方推荐) 2-个人项目(用户上传)',
  
  -- 项目描述
  `short_description` VARCHAR(200) COMMENT '简短描述（一句话介绍，列表页展示）',
  `detailed_description` TEXT COMMENT '详细描述（Markdown格式，详情页展示）',
  
  -- 项目链接
  `git_url` VARCHAR(200) COMMENT 'Git仓库地址',
  `demo_url` VARCHAR(200) COMMENT '在线演示地址',
  `video_url` VARCHAR(200) COMMENT '演示视频地址（可选）',
  
  -- 项目文件和图片
  `cover_image` VARCHAR(200) COMMENT '封面图URL（主预览图）',
  `screenshots` TEXT COMMENT '项目截图（JSON数组格式存储多张图片URL）',
  `project_file_url` VARCHAR(200) COMMENT '项目压缩包OSS地址',
  `file_size` BIGINT COMMENT '文件大小（字节）',
  
  -- 分类信息（关联 project_category 表）
  `category_id` BIGINT NOT NULL COMMENT '项目分类ID（关联project_category表）',
  `difficulty_level` TINYINT COMMENT '难度等级：1-入门 2-初级 3-中级 4-高级 5-专家',
  
  -- 开源与可见性
  `is_open_source` TINYINT NOT NULL DEFAULT 0 COMMENT '是否开源：0-私有（仅自己可见） 1-开源（所有人可见）',
  
  -- 状态管理
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-已删除 1-正常 2-草稿',
  `audit_status` TINYINT DEFAULT 0 COMMENT '审核状态：0-待审核 1-审核通过 2-审核拒绝（仅公开个人项目需要审核）',
  `audit_remark` VARCHAR(200) COMMENT '审核备注（拒绝时填写原因）',
  
  -- 基础统计
  `view_count` INT DEFAULT 0 COMMENT '浏览量',
  
  -- 作者信息（针对优秀开源项目，用于展示原作者）
  `original_author` VARCHAR(50) COMMENT '原作者名称（优秀开源项目展示用，如"尤雨溪"）',
  `original_author_role` VARCHAR(50) COMMENT '原作者角色（优秀开源项目展示用，如"Vue.js作者"）',
  `original_author_avatar` VARCHAR(200) COMMENT '原作者头像（优秀开源项目展示用）',
  
  -- GitHub数据（针对优秀开源项目）
  `github_stars` INT DEFAULT 0 COMMENT 'GitHub Star数（优秀项目展示用）',
  
  -- 时间字段
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  -- 索引
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_category_id` (`category_id`),
  INDEX `idx_project_type` (`project_type`),
  INDEX `idx_status` (`status`, `audit_status`),
  INDEX `idx_is_open_source` (`is_open_source`),
  INDEX `idx_created_at` (`created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目主表';