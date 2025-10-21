-- 分类表（支持两级分类结构）
DROP TABLE IF EXISTS coder_hub.category;

CREATE TABLE coder_hub.category (
    id VARCHAR(36) PRIMARY KEY COMMENT '分类ID（UUID）',
    category_name VARCHAR(50) NOT NULL COMMENT '分类名称',
    parent_id VARCHAR(36) DEFAULT NULL COMMENT '父分类ID，NULL表示一级分类',
    sort_order INT DEFAULT 0 COMMENT '排序序号，数字越小越靠前',
    description VARCHAR(200) DEFAULT NULL COMMENT '分类描述',
    icon VARCHAR(200) DEFAULT NULL COMMENT '分类图标URL（可选）',
    status INT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    article_count INT DEFAULT 0 COMMENT '文章数量（冗余字段，用于展示）',
    reference_count INT DEFAULT 0 COMMENT '引用量（被文章引用次数）',
    view_count BIGINT DEFAULT 0 COMMENT '阅读量（该分类下所有文章的总阅读量）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 索引
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status),
    INDEX idx_sort_order (sort_order),
    INDEX idx_create_time (create_time),
    
    -- 外键约束（自关联）
    FOREIGN KEY (parent_id) REFERENCES coder_hub.category(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章分类表';

-- 初始化一级分类数据
INSERT INTO coder_hub.category (id, category_name, parent_id, sort_order, description, status) VALUES
(UUID(), '后端秘籍', NULL, 1, '后端开发技术文章', 1),
(UUID(), '前端宝典', NULL, 2, '前端开发技术文章', 1),
(UUID(), '算法全书', NULL, 3, '算法与数据结构', 1),
(UUID(), '架构设计', NULL, 4, '系统架构与设计模式', 1),
(UUID(), '工具推荐', NULL, 5, '开发工具与效率提升', 1),
(UUID(), '秋招求职', NULL, 6, '求职面试经验分享', 1);

-- 初始化二级分类数据（示例）
-- 后端秘籍的子分类
SET @backend_id = (SELECT id FROM coder_hub.category WHERE category_name = '后端秘籍' AND parent_id IS NULL LIMIT 1);
INSERT INTO coder_hub.category (id, category_name, parent_id, sort_order, description, status) VALUES
(UUID(), 'Java开发', @backend_id, 1, 'Java技术栈文章', 1),
(UUID(), 'Golang学习', @backend_id, 2, 'Go语言开发', 1),
(UUID(), 'Python实战', @backend_id, 3, 'Python开发实践', 1),
(UUID(), 'Node.js', @backend_id, 4, 'Node.js后端开发', 1);

-- 前端宝典的子分类
SET @frontend_id = (SELECT id FROM coder_hub.category WHERE category_name = '前端宝典' AND parent_id IS NULL LIMIT 1);
INSERT INTO coder_hub.category (id, category_name, parent_id, sort_order, description, status) VALUES
(UUID(), 'Vue.js', @frontend_id, 1, 'Vue框架开发', 1),
(UUID(), 'React', @frontend_id, 2, 'React框架开发', 1),
(UUID(), 'JavaScript', @frontend_id, 3, 'JavaScript基础与进阶', 1),
(UUID(), 'CSS技巧', @frontend_id, 4, 'CSS样式与布局', 1);

-- 算法全书的子分类
SET @algorithm_id = (SELECT id FROM coder_hub.category WHERE category_name = '算法全书' AND parent_id IS NULL LIMIT 1);
INSERT INTO coder_hub.category (id, category_name, parent_id, sort_order, description, status) VALUES
(UUID(), '数据结构', @algorithm_id, 1, '常用数据结构详解', 1),
(UUID(), '算法基础', @algorithm_id, 2, '经典算法讲解', 1),
(UUID(), '刷题心得', @algorithm_id, 3, 'LeetCode刷题经验', 1);

-- 架构设计的子分类
SET @architecture_id = (SELECT id FROM coder_hub.category WHERE category_name = '架构设计' AND parent_id IS NULL LIMIT 1);
INSERT INTO coder_hub.category (id, category_name, parent_id, sort_order, description, status) VALUES
(UUID(), '微服务', @architecture_id, 1, '微服务架构设计', 1),
(UUID(), '分布式系统', @architecture_id, 2, '分布式技术', 1),
(UUID(), '设计模式', @architecture_id, 3, '常用设计模式', 1);

-- 工具推荐的子分类
SET @tools_id = (SELECT id FROM coder_hub.category WHERE category_name = '工具推荐' AND parent_id IS NULL LIMIT 1);
INSERT INTO coder_hub.category (id, category_name, parent_id, sort_order, description, status) VALUES
(UUID(), 'IDE工具', @tools_id, 1, '集成开发环境推荐', 1),
(UUID(), '版本控制', @tools_id, 2, 'Git等版本控制工具', 1),
(UUID(), '效率工具', @tools_id, 3, '提升开发效率的工具', 1);

-- 秋招求职的子分类
SET @job_id = (SELECT id FROM coder_hub.category WHERE category_name = '秋招求职' AND parent_id IS NULL LIMIT 1);
INSERT INTO coder_hub.category (id, category_name, parent_id, sort_order, description, status) VALUES
(UUID(), '面试经验', @job_id, 1, '面试题目与经验分享', 1),
(UUID(), '简历优化', @job_id, 2, '简历撰写技巧', 1),
(UUID(), 'Offer选择', @job_id, 3, '如何选择合适的Offer', 1);

