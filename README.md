# CoderHub - 开发者博客交流智能小站

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)
![SpringBoot](https://img.shields.io/badge/SpringBoot-3.2.5-brightgreen?logo=springboot)
![Vue](https://img.shields.io/badge/Vue-3.5-4FC08D?logo=vuedotjs)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql)
![Redis](https://img.shields.io/badge/Redis-Latest-DC382D?logo=redis)
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

目前仓库代码已完成 `v1.0.1` 版本的核心功能合并，系统整体运行稳定。
。
- **开发阶段 (当前重点)**：优化 RAG 检索的准确率与 Top-K 召回策略；管理端图表统计增强，降低AI幻觉，增强智能体交互能力，计划引入代码相关Agent功能。
---

### 本地开发指南

如果你希望在本地运行 CoderHub，请确保你的本地已配置以下环境：

#### 1. 环境准备
- **Java**: JDK 17+
- **Node.js**: v20.x 或更高 (推荐使用 pnpm / npm)
- **MySQL**: 8.0+
- **Redis**: 推荐 6.0+
- **对象存储**: Aliyun OSS +本地 MinIO (可在配置文件中切换)，Minio可用于存储大文件
- **AI 模型**: 需准备通义千问 (DashScope) 或 OpenAI 的 API Key

#### 2. 后端部署步骤
1. 克隆仓库：`git clone https://github.com/Ea1XonCodeHou/CoderHub.git`
2. 数据库初始化：运行 `coder_hub.sql` 脚本创建表结构及基础测试数据。
3. 修改配置：在 `coderhub-server/src/main/resources/application.yml` 中修改数据库、Redis、OSS 以及大模型的 API Key 信息。
4. 编译启动：在根目录运行 `./mvnw clean install`，随后启动 `CoderHubApplication`。

#### 3. 前端部署步骤
1. 进入前端目录：`cd coderhub_frontend`
2. 安装依赖：`npm install`
3. 启动项目：`npm run dev`
4. 访问地址：浏览器打开 `http://localhost:5173`。

---

### 关于部署

本项目目前已使用 **Aliyun ECS** 实现生产环境部署，并结合 **Docker** 容器化技术保证了服务的一致性。
- **演示地址**：[http://8.140.19.7/](http://8.140.19.7/)
- **部署方案**：Spring Boot 被打包为 Jar 包镜像，通过 Docker-compose 统一编排，前端 Vite 项目经 Nginx 构建后对外暴露。

### 参与贡献

非常欢迎任何形式的 Contrubution！如果你在本地部署时候发现了任何Bug 或者有更好的功能建议，欢迎提出 Issue 或联系作者。

### 关于作者

- **学校**: WHU-CS
- **作者**: Ea1XonCodeHou
- **邮箱**: 975292748@qq.com

### 开源协议

本项目基于 [MIT License](LICENSE) 开源。

