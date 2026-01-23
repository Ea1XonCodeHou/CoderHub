-- =========================================
-- 优秀开源项目数据插入脚本
-- 包含6个真实的GitHub高赞开源项目
-- =========================================

-- 插入优秀开源项目（project_type = 1）
-- 注意：需要一个管理员账号，这里使用占位符，请替换为实际的管理员user_id

SET @admin_user_id = 'caadf00b-473e-4ec3-816d-9b373b2550ad'; -- 请替换为实际的管理员user_id

-- ==================== 1. Vue.js ====================
INSERT INTO project (
    id, user_id, project_name, project_type, short_description, detailed_description,
    git_url, demo_url, cover_image, screenshots, category_id, difficulty_level,
    is_open_source, status, audit_status, view_count,
    original_author, original_author_role, original_author_avatar, github_stars,
    created_at, updated_at
) VALUES (
    'showcase-vue-001',
    @admin_user_id,
    'Vue.js',
    1, -- 优秀开源项目
    '渐进式JavaScript框架，易学易用，性能出色，适用于构建Web界面',
    '# Vue.js - 渐进式JavaScript框架

![Vue.js](https://img.shields.io/badge/Vue.js-3.x-brightgreen)
![Stars](https://img.shields.io/github/stars/vuejs/core?style=social)
![License](https://img.shields.io/badge/License-MIT-blue)

## 项目简介

Vue (读音 /vjuː/，类似于 view) 是一套用于构建用户界面的渐进式JavaScript框架。与其它大型框架不同的是，Vue 被设计为可以自底向上逐层应用。Vue 的核心库只关注视图层，不仅易于上手，还便于与第三方库或既有项目整合。

## 核心特性

- 🚀 **易学易用** - 简洁的API设计，上手快速
- 📦 **渐进式框架** - 可以只使用核心库，也可以配合生态系统使用
- ⚡️ **性能出色** - 20KB min+gzip 运行大小，超快虚拟DOM
- 🛠️ **丰富的生态** - Vue Router、Vuex/Pinia、DevTools等
- 💪 **TypeScript支持** - 完整的TypeScript类型支持

## 适用场景

- 单页应用(SPA)
- 渐进式Web应用(PWA)
- 服务端渲染(SSR)
- 静态站点生成(SSG)
- 移动端混合应用

## 快速开始

```bash
# 使用Vite创建Vue 3项目
npm create vite@latest my-vue-app -- --template vue

# 进入项目目录
cd my-vue-app

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

## 核心概念

### 声明式渲染
```vue
<script setup>
import { ref } from ''vue''
const message = ref(''Hello Vue!'')
</script>

<template>
  <h1>{{ message }}</h1>
</template>
```

### 响应式系统
Vue 3 使用基于 Proxy 的响应式系统，提供更好的性能和更完整的语言特性支持。

### 组合式API
Composition API 提供了更灵活的代码组织方式，更好的TypeScript支持。

## 社区与生态

- 📚 官方文档：https://vuejs.org
- 💬 Discord社区
- 🎓 Vue Mastery / Vue School 在线课程
- 🔌 超过2000+的插件和组件库

## 许可证

MIT License - 由尤雨溪(Evan You)创建和维护',
    'https://github.com/vuejs/core',
    'https://vuejs.org',
    'https://vuejs.org/images/logo.png',
    '[]',
    1, -- Web应用
    3, -- 中级
    1, -- 开源
    1, -- 正常
    1, -- 审核通过
    0, -- 初始浏览量
    'Evan You',
    'Creator & Project Lead',
    'https://avatars.githubusercontent.com/u/499550',
    45000, -- GitHub Stars (实际约45k+)
    NOW(),
    NOW()
);

-- Vue.js 技术栈关联
INSERT INTO project_tech_relation (project_id, tech_id, created_at) VALUES
('showcase-vue-001', 1, NOW()),  -- Vue 3
('showcase-vue-001', 5, NOW()),  -- TypeScript
('showcase-vue-001', 13, NOW()), -- Vite
('showcase-vue-001', 64, NOW()); -- RESTful API

-- ==================== 2. Spring Boot ====================
INSERT INTO project (
    id, user_id, project_name, project_type, short_description, detailed_description,
    git_url, demo_url, cover_image, screenshots, category_id, difficulty_level,
    is_open_source, status, audit_status, view_count,
    original_author, original_author_role, original_author_avatar, github_stars,
    created_at, updated_at
) VALUES (
    'showcase-springboot-002',
    @admin_user_id,
    'Spring Boot',
    1,
    '基于Spring的快速应用开发框架，简化Spring应用的初始搭建和开发过程',
    '# Spring Boot - 简化Spring应用开发

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)
![Stars](https://img.shields.io/github/stars/spring-projects/spring-boot?style=social)
![License](https://img.shields.io/badge/License-Apache%202.0-blue)

## 项目简介

Spring Boot 是由 Pivotal 团队提供的全新框架，旨在简化 Spring 应用的初始搭建和开发过程。该框架使用了特定的方式来进行配置，从而使开发人员不再需要定义样板化的配置。

## 核心特性

- 🚀 **快速启动** - 创建独立的Spring应用程序
- 📦 **自动配置** - 根据类路径自动配置Spring
- 🔧 **零配置** - 无需XML配置
- 📊 **生产就绪** - 内置健康检查、指标监控
- 🌐 **嵌入式服务器** - 内嵌Tomcat、Jetty或Undertow
- 🛠️ **开发工具** - 自动重启、LiveReload等

## 技术栈

- Java 17+
- Spring Framework 6
- Maven / Gradle
- JPA / MyBatis
- Spring Security
- Spring Cloud

## 快速开始

```java
@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}

@RestController
public class HelloController {
    @GetMapping("/hello")
    public String hello() {
        return "Hello Spring Boot!";
    }
}
```

## 应用场景

- 微服务架构
- RESTful API开发
- 企业级应用
- 云原生应用
- 数据处理管道

## 生态系统

- Spring Cloud - 微服务解决方案
- Spring Data - 数据访问
- Spring Security - 安全框架
- Spring Batch - 批处理
- Spring Integration - 企业集成

## 社区

- 官方文档：https://spring.io/projects/spring-boot
- Stack Overflow
- Spring Community Forums
- GitHub Discussions

## 许可证

Apache License 2.0',
    'https://github.com/spring-projects/spring-boot',
    'https://spring.io/projects/spring-boot',
    'https://spring.io/img/spring-2.svg',
    '[]',
    1, -- Web应用
    4, -- 高级
    1,
    1,
    1,
    0,
    'Pivotal Team',
    'Spring Core Team',
    'https://avatars.githubusercontent.com/u/317776',
    72000, -- 约72k stars
    NOW(),
    NOW()
);

INSERT INTO project_tech_relation (project_id, tech_id, created_at) VALUES
('showcase-springboot-002', 24, NOW()), -- Java
('showcase-springboot-002', 25, NOW()), -- Spring Boot
('showcase-springboot-002', 27, NOW()), -- MySQL
('showcase-springboot-002', 64, NOW()); -- RESTful API

-- ==================== 3. React ====================
INSERT INTO project (
    id, user_id, project_name, project_type, short_description, detailed_description,
    git_url, demo_url, cover_image, screenshots, category_id, difficulty_level,
    is_open_source, status, audit_status, view_count,
    original_author, original_author_role, original_author_avatar, github_stars,
    created_at, updated_at
) VALUES (
    'showcase-react-003',
    @admin_user_id,
    'React',
    1,
    '用于构建用户界面的JavaScript库，由Facebook开发和维护',
    '# React - 构建用户界面的JavaScript库

![React](https://img.shields.io/badge/React-18.x-blue)
![Stars](https://img.shields.io/github/stars/facebook/react?style=social)
![License](https://img.shields.io/badge/License-MIT-blue)

## 项目简介

React 是一个用于构建用户界面的 JavaScript 库，由 Facebook 开发和维护。它采用声明式设计，使创建交互式 UI 变得轻而易举。

## 核心特性

- 🎯 **声明式** - 设计简洁的视图，React能高效更新渲染组件
- 🧩 **组件化** - 构建管理自身状态的封装组件
- ⚡️ **高效** - 虚拟DOM机制，最小化DOM操作
- 🔄 **一次学习，随处编写** - React Native跨平台开发
- 🪝 **Hooks** - 无需class即可使用state等React特性

## 快速开始

```bash
# 创建React应用
npx create-react-app my-app
cd my-app
npm start
```

## 基础示例

```jsx
function Welcome() {
  const [count, setCount] = useState(0);
  
  return (
    <div>
      <h1>Hello, React!</h1>
      <button onClick={() => setCount(count + 1)}>
        Clicked {count} times
      </button>
    </div>
  );
}
```

## 生态系统

- React Router - 路由管理
- Redux / MobX - 状态管理
- Next.js - React框架
- React Native - 移动端开发
- Material-UI / Ant Design - UI组件库

## 应用场景

- 单页应用(SPA)
- 渐进式Web应用(PWA)
- 移动应用(React Native)
- 桌面应用(Electron)
- 服务端渲染(Next.js)

## 学习资源

- 官方文档：https://react.dev
- React官方教程
- FreeCodeCamp
- Egghead.io

## 许可证

MIT License - 由Facebook开源',
    'https://github.com/facebook/react',
    'https://react.dev',
    'https://react.dev/images/og-learn.png',
    '[]',
    1, -- Web应用
    3, -- 中级
    1,
    1,
    1,
    0,
    'Meta (Facebook)',
    'Open Source Team',
    'https://avatars.githubusercontent.com/u/69631',
    220000, -- 约220k stars
    NOW(),
    NOW()
);

INSERT INTO project_tech_relation (project_id, tech_id, created_at) VALUES
('showcase-react-003', 2, NOW()),  -- React
('showcase-react-003', 3, NOW()),  -- JavaScript
('showcase-react-003', 5, NOW()),  -- TypeScript
('showcase-react-003', 64, NOW()); -- RESTful API

-- ==================== 4. Kubernetes ====================
INSERT INTO project (
    id, user_id, project_name, project_type, short_description, detailed_description,
    git_url, demo_url, cover_image, screenshots, category_id, difficulty_level,
    is_open_source, status, audit_status, view_count,
    original_author, original_author_role, original_author_avatar, github_stars,
    created_at, updated_at
) VALUES (
    'showcase-k8s-004',
    @admin_user_id,
    'Kubernetes',
    1,
    '开源的容器编排引擎，自动化容器化应用的部署、扩展和管理',
    '# Kubernetes (K8s) - 容器编排平台

![Kubernetes](https://img.shields.io/badge/Kubernetes-v1.28-blue)
![Stars](https://img.shields.io/github/stars/kubernetes/kubernetes?style=social)
![License](https://img.shields.io/badge/License-Apache%202.0-blue)

## 项目简介

Kubernetes（K8s）是一个开源的容器编排引擎，用于自动化容器化应用程序的部署、扩展和管理。它最初由 Google 设计，现在由云原生计算基金会（CNCF）维护。

## 核心特性

- 🚀 **自动部署和回滚** - 平滑升级应用
- 📦 **服务发现和负载均衡** - 自动服务注册与发现
- 💾 **存储编排** - 自动挂载存储系统
- 🔧 **自我修复** - 自动重启、替换、杀死失败容器
- 🔒 **密钥和配置管理** - 安全管理敏感信息
- ⚖️ **水平扩展** - 根据负载自动扩缩容

## 核心概念

### Pod
最小部署单元，包含一个或多个容器

### Service
定义Pod的逻辑集合和访问策略

### Deployment
声明式更新Pod和ReplicaSet

### Namespace
虚拟集群，实现资源隔离

## 快速开始

```bash
# 使用Minikube本地运行K8s
minikube start

# 部署应用
kubectl create deployment hello-k8s --image=nginx
kubectl expose deployment hello-k8s --type=NodePort --port=80

# 查看服务
kubectl get services
```

## 架构组件

- **Control Plane**: API Server、Scheduler、Controller Manager、etcd
- **Node**: Kubelet、Kube-proxy、Container Runtime

## 应用场景

- 微服务架构
- CI/CD管道
- 多云部署
- 边缘计算
- 大数据处理

## 生态系统

- Helm - 包管理器
- Istio - 服务网格
- Prometheus - 监控
- Fluentd - 日志收集
- Rancher - 集群管理

## 学习资源

- 官方文档：https://kubernetes.io
- CNCF课程
- Kubernetes The Hard Way
- KubeCon大会

## 许可证

Apache License 2.0 - CNCF项目',
    'https://github.com/kubernetes/kubernetes',
    'https://kubernetes.io',
    'https://kubernetes.io/images/kubernetes-horizontal-color.png',
    '[]',
    5, -- 云原生/DevOps
    5, -- 专家级
    1,
    1,
    1,
    0,
    'Google & CNCF',
    'Cloud Native Computing Foundation',
    'https://avatars.githubusercontent.com/u/13629408',
    107000, -- 约107k stars
    NOW(),
    NOW()
);

INSERT INTO project_tech_relation (project_id, tech_id, created_at) VALUES
('showcase-k8s-004', 40, NOW()), -- Go
('showcase-k8s-004', 44, NOW()), -- Docker
('showcase-k8s-004', 45, NOW()); -- Kubernetes

-- ==================== 5. Elasticsearch ====================
INSERT INTO project (
    id, user_id, project_name, project_type, short_description, detailed_description,
    git_url, demo_url, cover_image, screenshots, category_id, difficulty_level,
    is_open_source, status, audit_status, view_count,
    original_author, original_author_role, original_author_avatar, github_stars,
    created_at, updated_at
) VALUES (
    'showcase-es-005',
    @admin_user_id,
    'Elasticsearch',
    1,
    '分布式、RESTful风格的搜索和数据分析引擎',
    '# Elasticsearch - 搜索和分析引擎

![Elasticsearch](https://img.shields.io/badge/Elasticsearch-8.x-blue)
![Stars](https://img.shields.io/github/stars/elastic/elasticsearch?style=social)
![License](https://img.shields.io/badge/License-SSPL-yellow)

## 项目简介

Elasticsearch 是一个分布式、RESTful 风格的搜索和数据分析引擎，能够解决不断涌现出的各种用例。作为 Elastic Stack 的核心，它集中存储您的数据，帮助您发现意料之中以及意料之外的情况。

## 核心特性

- 🔍 **全文搜索** - 强大的全文检索能力
- 📊 **实时分析** - 近实时的数据分析
- 🌐 **分布式架构** - 水平扩展能力
- 🔄 **RESTful API** - 简单易用的HTTP接口
- 💾 **文档存储** - JSON文档存储
- 🎯 **相关性打分** - 精准的搜索结果排序

## 核心概念

### Index (索引)
类似关系数据库的表

### Document (文档)
JSON格式的数据记录

### Mapping (映射)
定义文档和字段的存储方式

### Query DSL
强大的查询语言

## 快速开始

```bash
# 使用Docker启动ES
docker run -d -p 9200:9200 -p 9300:9300 \\
  -e "discovery.type=single-node" \\
  elasticsearch:8.11.0

# 创建索引
curl -X PUT "localhost:9200/my_index"

# 索引文档
curl -X POST "localhost:9200/my_index/_doc/1" \\
  -H ''Content-Type: application/json'' \\
  -d ''{"title": "Hello ES", "content": "First document"}''

# 搜索
curl -X GET "localhost:9200/my_index/_search?q=hello"
```

## 应用场景

- 全文搜索引擎
- 日志和事件数据分析
- 应用性能监控(APM)
- 地理位置数据分析
- 安全分析
- 业务分析

## Elastic Stack

- **Kibana** - 数据可视化
- **Logstash** - 数据采集
- **Beats** - 轻量级数据采集器

## 查询示例

```json
{
  "query": {
    "bool": {
      "must": [
        {"match": {"title": "elasticsearch"}},
        {"range": {"date": {"gte": "2024-01-01"}}}
      ]
    }
  }
}
```

## 集群架构

- Master节点 - 集群管理
- Data节点 - 数据存储和搜索
- Coordinating节点 - 请求路由
- Ingest节点 - 数据预处理

## 学习资源

- 官方文档：https://www.elastic.co/guide
- Elastic认证课程
- Elastic社区论坛

## 许可证

Server Side Public License (SSPL) / Elastic License',
    'https://github.com/elastic/elasticsearch',
    'https://www.elastic.co/elasticsearch/',
    'https://static-www.elastic.co/v3/assets/bltefdd0b53724fa2ce/blt4466841eed0bf232/5d082a34d8ff351753cbc99f/logo-elasticsearch-32-color.svg',
    '[]',
    6, -- 数据分析
    4, -- 高级
    1,
    1,
    1,
    0,
    'Elastic NV',
    'Elastic Company',
    'https://avatars.githubusercontent.com/u/6764390',
    68000, -- 约68k stars
    NOW(),
    NOW()
);

INSERT INTO project_tech_relation (project_id, tech_id, created_at) VALUES
('showcase-es-005', 24, NOW()), -- Java
('showcase-es-005', 33, NOW()), -- Elasticsearch
('showcase-es-005', 64, NOW()); -- RESTful API

-- ==================== 6. PyTorch ====================
INSERT INTO project (
    id, user_id, project_name, project_type, short_description, detailed_description,
    git_url, demo_url, cover_image, screenshots, category_id, difficulty_level,
    is_open_source, status, audit_status, view_count,
    original_author, original_author_role, original_author_avatar, github_stars,
    created_at, updated_at
) VALUES (
    'showcase-pytorch-006',
    @admin_user_id,
    'PyTorch',
    1,
    '开源机器学习框架，基于Python，提供两大核心功能：张量计算和深度神经网络',
    '# PyTorch - 深度学习框架

![PyTorch](https://img.shields.io/badge/PyTorch-2.x-red)
![Stars](https://img.shields.io/github/stars/pytorch/pytorch?style=social)
![License](https://img.shields.io/badge/License-BSD--3-blue)

## 项目简介

PyTorch 是一个开源的机器学习框架，基于 Python 编程语言和 Torch 库。它主要由 Facebook 的人工智能研究团队开发，为深度学习研究提供了灵活性和速度。

## 核心特性

- 🔥 **动态计算图** - 灵活的计算图构建
- 🐍 **Python优先** - 原生Python支持
- ⚡️ **GPU加速** - CUDA支持，高效计算
- 🧠 **神经网络** - 丰富的神经网络模块
- 📊 **自动微分** - 自动梯度计算
- 🔄 **易于调试** - 标准Python调试工具

## 快速开始

```python
import torch
import torch.nn as nn

# 创建张量
x = torch.tensor([[1, 2], [3, 4]], dtype=torch.float32)

# 定义神经网络
class Net(nn.Module):
    def __init__(self):
        super(Net, self).__init__()
        self.fc1 = nn.Linear(784, 128)
        self.fc2 = nn.Linear(128, 10)
    
    def forward(self, x):
        x = torch.relu(self.fc1(x))
        x = self.fc2(x)
        return x

model = Net()

# 训练模型
optimizer = torch.optim.SGD(model.parameters(), lr=0.01)
criterion = nn.CrossEntropyLoss()
```

## 核心组件

### Tensor (张量)
多维数组，支持GPU加速

### Autograd (自动微分)
自动计算梯度

### nn.Module
神经网络模块基类

### Optim (优化器)
各种优化算法实现

## 应用场景

- 计算机视觉
- 自然语言处理
- 语音识别
- 强化学习
- 生成对抗网络(GAN)
- 推荐系统

## 生态系统

- **TorchVision** - 计算机视觉
- **TorchText** - 自然语言处理
- **TorchAudio** - 音频处理
- **PyTorch Lightning** - 高层API
- **Detectron2** - 目标检测

## 预训练模型

```python
import torchvision.models as models

# 加载预训练ResNet
resnet = models.resnet50(pretrained=True)
```

## 分布式训练

支持多GPU、多机分布式训练：
- DataParallel
- DistributedDataParallel
- RPC框架

## 部署

- TorchScript - 生产环境部署
- ONNX - 跨框架模型交换
- TorchServe - 模型服务化

## 学习资源

- 官方文档：https://pytorch.org/docs
- PyTorch Tutorials
- Deep Learning with PyTorch
- Fast.ai课程

## 社区

- PyTorch Forums
- GitHub Discussions
- Stack Overflow
- Discord频道

## 许可证

BSD-3-Clause License - 由Meta AI开发',
    'https://github.com/pytorch/pytorch',
    'https://pytorch.org',
    'https://pytorch.org/assets/images/pytorch-logo.png',
    '[]',
    7, -- AI/机器学习
    4, -- 高级
    1,
    1,
    1,
    0,
    'Meta AI (Facebook AI Research)',
    'Research Team',
    'https://avatars.githubusercontent.com/u/21003710',
    79000, -- 约79k stars
    NOW(),
    NOW()
);

INSERT INTO project_tech_relation (project_id, tech_id, created_at) VALUES
('showcase-pytorch-006', 26, NOW()), -- Python
('showcase-pytorch-006', 53, NOW()), -- PyTorch
('showcase-pytorch-006', 55, NOW()); -- TensorFlow/ML

-- =========================================
-- 使用说明：
-- 1. 将上面的 @admin_user_id 替换为实际的管理员用户ID
-- 2. 如果需要使用真实的封面图，可以替换cover_image字段的URL
-- 3. 可以根据实际的tech_stack_dict表中的ID调整技术栈关联
-- 4. GitHub Stars数据是基于2024年的大概数值，实际可能更高
-- =========================================

