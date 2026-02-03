# CoderHub - 开发者博客交流智能小站

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)
![SpringBoot](https://img.shields.io/badge/SpringBoot-3.2.5-brightgreen?logo=springboot)
![Vue](https://img.shields.io/badge/Vue-3.5-4FC08D?logo=vuedotjs)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql)
![Redis](https://img.shields.io/badge/Redis-Latest-DC382D?logo=redis)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.13-FF6600?logo=rabbitmq)
![LangChain4j](https://img.shields.io/badge/LangChain4j-0.35.0-blue)
![Vite](https://img.shields.io/badge/Vite-7.1-646CFF?logo=vite)

CoderHub 是一个面向开发/学习者的智能技术博客交流社区平台。不仅提供了体系化的技术博客阅读/发布功能，和项目/教程学习模块，更整合了目前热门的 AI Agent 能力，让开发者在阅读和学习过程中，能通过智能助手实时调取平台内的知识库，实现“所见即所得”的互动式学习体验。

🚀 **线上演示地址**：[http://8.140.19.7/](http://8.140.19.7/)

### 项目演示预览

| 概览展示 | 内容详情 | AI 交互 |
| :---: | :---: | :---: |
| ![Preview 1](https://eaxon-bucket.oss-cn-wuhan-lr.aliyuncs.com/dis-1.png) |![Preview 3](https://eaxon-bucket.oss-cn-wuhan-lr.aliyuncs.com/dis3.png)  | ![Preview 2](https://eaxon-bucket.oss-cn-wuhan-lr.aliyuncs.com/dis-2.png) |

---

### 核心技术选型

在构建 CoderHub 的过程中，我们并没有盲目追求框架的数量，而是更看重组件间的协同效率与对 AI 场景的支持。

*   **后端驱动**：基于 **Java 17** 与 **Spring Boot 3.2.5** 构建，利用其成熟的生态处理高并发请求。持久化层选用 MyBatis 配合 MySQL，而 Redis 则承担了分布式缓存与高频数据暂存的任务。
*   **AI Agent 内核**：这是项目的核心灵魂。我们采用了最新的 **LangChain4j (0.35.0)** 框架，它不仅能对接主流大模型，更重要的是其提供的 **Tool Calling** 机制，使得 AI 能够动态调用后端定义的搜索工具，精准检索本地数据库中的文章与教程。
*   **现代前端交互**：前端基于 **Vue 3** + **Vite** 体系，引入 **Pinia** 进行全局状态管理。为了支撑 AI 的流式响应输出逻辑，我们深度定制了聊天组件，配合 `marked` 与 `highlight.js` 实现了丝滑的 Markdown 渲染。
*   **云原生存储集成**：针对技术文档的多样性，项目打通了 **Aliyun OSS** 与 **MinIO**。这种双重选择让我们既能在公有云上获得高性能的视频流化体验，也能在本地环境中进行低成本的开发测试。

### 核心功能模块

项目的设计初衷是“高效、体系化、智能化”，功能模块也紧紧围绕这三个关键词展开。

**1. 智能化技术助理 (Hub-AI)**
不同于普通的对话机器人，CoderHub 的 AI 助手拥有“全局视野”（也就是RAG检索功能）。例如当你问它“如何学习微服务”时，它会自动在平台内检索相关的实战项目和技术教程，并将精准的链接与摘要直接推送到对话流中。

**2. 体系化教程 (Systematic Tutorial)**
平台教程模块不仅支持零散的文章发布，更侧重于组织具备逻辑顺序的“章节式教程”。每个教程都支持挂载视频课时与 PDF 附件。借助内置的多媒体播放器与 PDF 阅读器，用户无需跳转三方平台即可完成闭环学习。

**3. 开发者动态与博客**
支持全量的 Markdown 写作体验。为了极致的性能，我们采用了“元数据入库、正文存对象存储”的方案，显著降低了长文本对数据库带来的压力。

**4. 自动化管理后台**
为管理员账户提供了一站式的仪表盘监控，包括内容的审核统计、用户活跃度分析以及 AI 工具调用的实时日志追踪。方便后台宏观调控，并带有ChromaDB联通测试/相似度查询测试接口方便管理员智能监控。

### 项目开发进度

目前仓库代码已完成 `v1.0.3` 版本的核心功能合并，系统整体运行稳定。

- **基础设施更新**：完成 Docker 容器化重构，支持环境变量脱敏配置。
- **CI/CD 集成**：集成 GitHub Actions，实现代码推送自动部署。
- **开发阶段 (当前重点)**：优化 RAG 检索的准确率与 Top-K 召回策略；管理端图表统计增强，降低AI幻觉，增强智能体交互能力，计划引入代码相关Agent功能。

---

### 更新日志

#### v1.0.3 (2026-02-03) - 消息通知系统

**🔔 新增消息通知功能**

本次更新引入了完整的消息通知系统，采用 **RabbitMQ** 消息队列实现异步解耦的生产者-消费者架构：

- **消息类型支持**：
  - 社区消息：点赞通知、评论通知、关注通知
  - 系统消息：文章审核通过通知、系统警告通知

- **技术实现**：
  - 引入 RabbitMQ 作为消息中间件，采用 Topic Exchange 实现灵活路由
  - 生产者：在点赞/评论/关注/审核等业务操作完成后异步发送消息事件
  - 消费者：监听队列消费消息，构建通知内容并持久化入库
  - 使用 Jackson JSON 序列化，避免 Java 序列化安全限制

- **前端交互**：
  - 导航栏新增消息铃铛图标，实时显示未读消息数量角标
  - 点击弹出悬浮通知列表，支持社区/系统消息分类切换
  - 支持单条消息标记已读（已读即删除）和一键清空功能
  - 30秒轮询机制保持消息实时性

- **基础设施**：
  - RabbitMQ 已集成至 `docker-compose.yml`，支持本地 Docker 一键启动
  - 新增 `RABBITMQ_USER` / `RABBITMQ_PASSWORD` 环境变量配置
  - 兼容现有 CI/CD 流程，生产环境通过 Docker 网络内部通信

#### v1.0.2 - 基础功能完善

- 完成 Docker 容器化重构
- 集成 GitHub Actions CI/CD
- RAG 检索优化与管理端增强

---

### 本地开发指南

如果你希望在本地运行 CoderHub，请确保你的本地已配置以下环境：

#### 1. 环境准备
- **Java**: JDK 17+
- **Node.js**: v20.x 或更高
- **Docker Desktop**: 用于一键启动中间件（MySQL, Redis, MinIO, ChromaDB,RabbitMQ）
- **AI 模型**: 准备通义千问 (DashScope) 或 OpenAI 的 API Key

#### 2. 核心：隐私保护与配置
项目采用 **"代码与配置分离"** 方案，所有敏感信息（密码、密钥）均不存放在代码库中。
1. 在项目根目录创建 `.env` 文件（已加入 `.gitignore`）。
2. 参考项目根目录下的环境变量配置要求，填入你的真实 `DB_PASSWORD`、`OSS_AK`、`AI_KEY`、`RABBITMQ_USER`、`RABBITMQ_PASSWORD` 等。

#### 3. 启动中间件 (Docker)
在根目录下运行以下命令，即可一键拉起与生产环境 100% 一致的基础设施：
```powershell
docker-compose up -d mysql redis minio chroma rabbitmq
```

#### 4. 后端启动 (IDEA)
1. 在 IDEA 的启动配置中指定 `Active profiles: dev`。
2. 在 `Environment variables` 中加载 `.env` 里的变量（或使用 EnvFile 插件）。
3. 运行 `CoderHubApplication`。

#### 5. 前端启动
```bash
cd coderhub_frontend
npm install
npm run dev
```

---

### 关于部署与 CICD

本项目已实现标准化的 **Docker 容器化部署**，并支持基于 GitHub Actions 的 CICD 流程。

- **环境变量驱动**：服务器端只需维护一份独立的 `.env` 文件，镜像构建时会自动注入配置。
- **一键发布**：通过修改后的 `docker-compose.yml`，宿主机不再需要安装任何基础软件。
- **部署命令**：
  ```bash
  docker-compose up -d --build
  ```
- **演示地址**：[http://8.140.19.7/](http://8.140.19.7/)


### 参与贡献

非常欢迎任何形式的 Contrubution！如果你在本地部署时候发现了任何Bug 或者有更好的功能建议，欢迎提出 Issue 或联系作者。

### 关于作者

- **学校**: WHU-CS
- **作者**: Ea1XonCodeHou
- **邮箱**: 975292748@qq.com

### 开源协议

本项目基于 [MIT License](LICENSE) 开源。

