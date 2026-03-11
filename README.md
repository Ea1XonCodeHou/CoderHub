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

目前仓库代码已完成 `v1.0.5` 版本的核心功能合并，系统整体运行稳定。

- **基础设施更新**：完成 Docker 容器化重构，支持环境变量脱敏配置。
- **CI/CD 集成**：集成 GitHub Actions，实现代码推送自动部署。
- **开发阶段 (当前重点)**：优化 RAG 检索的准确率与 Top-K 召回策略；管理端图表统计增强，降低AI幻觉，增强智能体交互能力，计划引入代码相关Agent功能。

---

### 更新日志

#### v1.0.5 (2026-03-11) - AI 深度检索升级 & 用户体验完善

**🤖 AI 深度检索（RAG）全面升级**

- **全文阅读模式**：用户在对话中指定文章 ID，AI 将直接从 OSS 下载 Markdown 全文（最多 6000 字）进行深度解读，不再局限于向量片段；新增专用系统提示词，彻底消除"知识库中暂无内容"的误判问题
- **文章全文 Redis 缓存**：下载的文章全文写入 Redis（TTL 2 小时），同一篇文章的后续深度检索请求直接命中缓存，无需重复访问 OSS；使用 `synchronized(articleId.intern())` + Double-Check 保证并发安全，避免多线程竞争下载
- **HyDE + 混合召回 + 实时进度**：深度检索模式引入 HyDE 假设文档生成、向量 Top-K + MySQL 关键词 BM25 双路召回、RRF 融合排序；后端通过 SSE 推送真实检索进度（生成假设文档→向量检索→关键词召回→融合排序），前端动态展示步骤

**📋 管理端文章管理升级**

- **文章管理**：管理端"文章审核"升级为"文章管理"，新增文章列表管理 Tab，支持按关键词/状态检索，以及隐藏/显示/置顶/删除等完整 CRUD 操作

**📧 找回密码 / 邮箱验证码**

- 登录页新增"忘记密码"弹窗组件（3 步流程：输入邮箱 → 输入验证码 + 新密码 → 重置成功），通过 163 邮箱 SMTP 发送 6 位验证码，Redis 存储验证码（TTL 5 分钟），60 秒内同一邮箱不可重复发送

**✅ 文章审核自动向量化**

- 管理员审核通过文章后，后端自动异步触发向量入库流程（分块 + 10% 重叠 + Embedding），新内容无需手动重建即可被深度检索召回

#### v1.0.4 (2026-03-05) - AI 限流与体验优化

**🤖 AI 模块优化**

本次更新聚焦于 AI 助手的稳定性与使用体验改善，引入基于 Redis 的免费额度管控机制，同时修复了多个已知 Bug：

- **AI 提问额度管控**：
  - 普通用户（`user_level=0`）注册后拥有固定 **10 次**免费提问额度，存储于 Redis，永久有效
  - 高级用户（`user_level≥1`）不受限制，额度标记为"无限制"
  - 每次发送消息后后端原子扣减，额度耗尽后返回温馨提示流（不消耗真实 AI 调用），引导用户联系管理员
  - 聊天界面顶部工具栏实时展示剩余额度，耗尽时红色警示

- **管理端 AI 额度管理接口**：
  - `POST /admin/ai-quota/init-all`：一键批量为所有普通用户初始化额度，已有记录跳过
  - `GET /admin/ai-quota/user/{userId}`：查询单用户剩余额度
  - `PUT /admin/ai-quota/user/{userId}`：手动调整指定用户额度

- **模型列表精简**：移除 DeepSeek Chat 选项，保留通义千问三档模型（Plus / Turbo / Max）

**🐛 Bug 修复**

- **视频播放修复**：修正 MinIO 内网地址在前端无法解析的问题，将 `minio:9000` 地址转换为 Nginx 代理路径 `/minio/...`，视频播放恢复正常
- **登录闪退修复**：修复登录成功后被立即重定向回登录页的问题；通知轮询的 `401` 错误不再触发强制跳转，同时调整轮询启动时序，避免 token 尚未稳定时的竞争问题
- **通知模块修复**：修复 RabbitMQ 鉴权失败（`ACCESS_REFUSED`）导致消息无法消费、通知表始终为空的问题；优化 Redis 未读数初始化逻辑，防止并发写入覆盖

**✨ 新增功能**

- **系统通知广播**：管理端新增"通知管理"菜单，支持选择 Emoji 前缀 + 自定义内容，一键向全体用户广播系统通知，基于 RabbitMQ 异步发送
- **AI 模型选择器重构**：将原生 `<select>` 替换为自定义下拉组件，支持图标与描述展示，适配深色/浅色双主题

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
  - 轮询机制保持消息实时性

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

