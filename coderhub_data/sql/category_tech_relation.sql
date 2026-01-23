CREATE TABLE `category_tech_relation` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `category_id` BIGINT NOT NULL COMMENT '项目分类ID',
  `tech_id` BIGINT NOT NULL COMMENT '技术栈ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  
  UNIQUE KEY `uk_category_tech` (`category_id`, `tech_id`),
  INDEX `idx_category_id` (`category_id`),
  INDEX `idx_tech_id` (`tech_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目分类与技术栈关联表';

-- 插入关联数据
-- 1. Web应用
INSERT INTO `category_tech_relation` (`category_id`, `tech_id`) 
SELECT 1, id FROM tech_stack_dict WHERE tech_name IN (
  'Vue 3', 'React', 'Angular', 'TypeScript', 'JavaScript', 'HTML5', 'CSS3', 'Sass/Less', 
  'Vite', 'Webpack', 'Element Plus', 'Ant Design',
  'Spring Boot', 'Spring Cloud', 'Node.js', 'Express', 'Nest.js', 'Django', 'FastAPI', 
  'Flask', 'Go', 'Gin', '.NET Core', 'Java', 'Python', 'PHP',
  'MySQL', 'PostgreSQL', 'MongoDB', 'Redis', 'Oracle', 'SQLite', 'Elasticsearch',
  'Docker', 'Kubernetes', 'Git', 'Jenkins', 'Nginx', 'Linux', 'CI/CD',
  'GraphQL', 'WebSocket', 'RESTful API'
);

-- 2. 移动应用
INSERT INTO `category_tech_relation` (`category_id`, `tech_id`) 
SELECT 2, id FROM tech_stack_dict WHERE tech_name IN (
  'Flutter', 'React Native', 'Swift', 'Kotlin', 'Android', 'iOS', 'uni-app',
  'TypeScript', 'JavaScript',
  'Spring Boot', 'Node.js', 'Django', 'FastAPI', 'Go', 'Java', 'Python',
  'MySQL', 'PostgreSQL', 'MongoDB', 'Redis', 'SQLite',
  'Docker', 'Git', 'Nginx', 'RESTful API', 'WebSocket'
);

-- 3. 桌面应用
INSERT INTO `category_tech_relation` (`category_id`, `tech_id`) 
SELECT 3, id FROM tech_stack_dict WHERE tech_name IN (
  'Electron', 'TypeScript', 'JavaScript',
  'Java', 'Python', '.NET Core', 'C/C++',
  'SQLite', 'MySQL', 'PostgreSQL',
  'Git'
);

-- 4. 后端服务
INSERT INTO `category_tech_relation` (`category_id`, `tech_id`) 
SELECT 4, id FROM tech_stack_dict WHERE tech_name IN (
  'Spring Boot', 'Spring Cloud', 'Node.js', 'Express', 'Nest.js', 'Django', 'FastAPI', 
  'Flask', 'Go', 'Gin', '.NET Core', 'Java', 'Python', 'PHP',
  'MySQL', 'PostgreSQL', 'MongoDB', 'Redis', 'Oracle', 'Elasticsearch',
  'Docker', 'Kubernetes', 'Git', 'Jenkins', 'Nginx', 'Linux', 'CI/CD',
  'GraphQL', 'WebSocket', 'RESTful API'
);

-- 5. AI/机器学习
INSERT INTO `category_tech_relation` (`category_id`, `tech_id`) 
SELECT 5, id FROM tech_stack_dict WHERE tech_name IN (
  'PyTorch', 'TensorFlow', 'OpenCV', 'LangChain', 'LangChain4j', 'Scikit-learn', 'Hugging Face',
  'Python', 'Java',
  'FastAPI', 'Flask', 'Django', 'Spring Boot',
  'MySQL', 'PostgreSQL', 'MongoDB', 'Redis', 'Elasticsearch',
  'Docker', 'Kubernetes', 'Git', 'Nginx', 'Linux', 'RESTful API'
);

-- 6. 数据分析
INSERT INTO `category_tech_relation` (`category_id`, `tech_id`) 
SELECT 6, id FROM tech_stack_dict WHERE tech_name IN (
  'Python', 'PyTorch', 'TensorFlow', 'Scikit-learn',
  'Vue 3', 'React', 'TypeScript', 'JavaScript',
  'MySQL', 'PostgreSQL', 'MongoDB', 'Redis', 'Elasticsearch',
  'FastAPI', 'Flask', 'Django',
  'Docker', 'Git', 'RESTful API'
);

-- 7. 物联网
INSERT INTO `category_tech_relation` (`category_id`, `tech_id`) 
SELECT 7, id FROM tech_stack_dict WHERE tech_name IN (
  'Raspberry Pi', 'Arduino', 'ESP32', 'MQTT', 'Zigbee', 'C/C++',
  'Python', 'Node.js',
  'MySQL', 'MongoDB', 'Redis', 'SQLite',
  'Docker', 'Git', 'Linux', 'RESTful API'
);

-- 8. 区块链
INSERT INTO `category_tech_relation` (`category_id`, `tech_id`) 
SELECT 8, id FROM tech_stack_dict WHERE tech_name IN (
  'Go', 'Java', 'Python', 'Node.js', 'TypeScript',
  'MySQL', 'PostgreSQL', 'MongoDB', 'Redis',
  'Docker', 'Kubernetes', 'Git', 'Linux', 'RESTful API'
);

-- 9. 游戏开发
INSERT INTO `category_tech_relation` (`category_id`, `tech_id`) 
SELECT 9, id FROM tech_stack_dict WHERE tech_name IN (
  'C/C++', 'JavaScript', 'TypeScript',
  'MySQL', 'MongoDB', 'Redis', 'SQLite',
  'Git', 'WebSocket'
);

-- 10. 其他（可以选择所有技术栈）
INSERT INTO `category_tech_relation` (`category_id`, `tech_id`) 
SELECT 10, id FROM tech_stack_dict;

