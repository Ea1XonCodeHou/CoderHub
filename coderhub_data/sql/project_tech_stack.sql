CREATE TABLE `tech_stack_dict` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `tech_name` VARCHAR(50) NOT NULL COMMENT '技术名称：Vue 3, Spring Boot等',
  `is_active` TINYINT DEFAULT 1 COMMENT '是否启用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  
  UNIQUE KEY `uk_tech_name` (`tech_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技术栈字典表';

-- 插入默认技术栈数据
INSERT INTO `tech_stack_dict` (`tech_name`) VALUES
-- 前端技术
('Vue 3'),
('React'),
('Angular'),
('TypeScript'),
('JavaScript'),
('HTML5'),
('CSS3'),
('Sass/Less'),
('Vite'),
('Webpack'),
('Element Plus'),
('Ant Design'),

-- 后端技术
('Spring Boot'),
('Spring Cloud'),
('Node.js'),
('Express'),
('Nest.js'),
('Django'),
('FastAPI'),
('Flask'),
('Go'),
('Gin'),
('.NET Core'),
('Java'),
('Python'),
('PHP'),

-- 数据库
('MySQL'),
('PostgreSQL'),
('MongoDB'),
('Redis'),
('Oracle'),
('SQLite'),
('Elasticsearch'),

-- AI/机器学习
('PyTorch'),
('TensorFlow'),
('OpenCV'),
('LangChain'),
('LangChain4j'),
('Scikit-learn'),
('Hugging Face'),

-- 移动开发
('Flutter'),
('React Native'),
('Swift'),
('Kotlin'),
('Android'),
('iOS'),
('uni-app'),

-- 工具/DevOps
('Docker'),
('Kubernetes'),
('Git'),
('Jenkins'),
('Nginx'),
('Linux'),
('CI/CD'),

-- 硬件/物联网
('Raspberry Pi'),
('Arduino'),
('ESP32'),
('MQTT'),
('Zigbee'),
('C/C++'),

-- 其他
('微信小程序'),
('GraphQL'),
('WebSocket'),
('RESTful API'),
('Electron');