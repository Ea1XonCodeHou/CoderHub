# CoderHub 服务器部署指南 (Standard Edition)

仓库内本文档旨在指导开发者如何将 CoderHub 项目快速部署到私有服务器。项目采用 Docker 容器化方案，实现了“代码与配置分离”，确保了部署的安全性和一致性。

---

## 🚀 一、部署环境要求

在开始之前，请确保您的服务器已安装以下软件：
- **Docker**: 20.10+
- **Docker Compose**: v2.0+ (通常现代 Docker 已内置 `docker compose` 命令)
- **Git**: 用于拉取代码

---

## 📁 二、项目部署结构

部署完成后，建议的项目路径结构如下：
```text
/opt/coderhub/               # 建议安装目录
├── .env                     # 🔑 必须手动创建：存放所有环境变量和密码
├── docker-compose.yml       # 容器编排文件
├── mysql/                   # 数据库持久化及初始化脚本
├── redis/                   # 缓存数据
├── minio/                   # 对象存储数据
├── chroma/                  # 向量数据库数据
├── coderhub_backend/        # 后端源码及 Dockerfile
└── coderhub_frontend/       # 前端源码及 Dockerfile
```

---

## 📋 三、部署步骤

### 1. 克隆代码库
```bash
git clone https://github.com/YourUsername/CoderHub.git /opt/coderhub
cd /opt/coderhub
```

### 2. 配置环境变量 (关键步)
项目**不再**在代码中硬编码任何密码。您必须在根目录创建一个 `.env` 文件。

```bash
# 创建并编辑环境文件
touch .env
nano .env
```

**`.env` 文件模板内容（请根据您的实际情况修改）：**
```env
# 数据库密码
DB_PASSWORD=your_strong_password

# 对象存储配置 (Aliyun OSS 或 MinIO)
OSS_ENDPOINT=oss-cn-hangzhou.aliyuncs.com
OSS_ACCESS_KEY_ID=your_aliyun_ak
OSS_ACCESS_KEY_SECRET=your_aliyun_sk
OSS_BUCKET_NAME=your_bucket_name

# MinIO 配置 (用于本地存储)
MINIO_USER=admin
MINIO_PASSWORD=your_minio_password

# AI 模块配置
AI_API_KEY=your_dashscope_api_key
```

### 3. 一键构建与启动
使用 Docker Compose 自动完成 Maven 编译、Vue 构建及容器拉起：
```bash
# 使用新版命令
docker compose up -d --build

# 或者旧版命令 (取决于服务器安装情况)
docker-compose up -d --build
```

---

## 🔍 四、访问与验证

部署成功后，服务暴露情况如下：
- **前端页面**：访问 `http://服务器IP` (默认 80 端口)
- **后端 API**：访问 `http://服务器IP/api/` (已通过 Nginx 反向代理)
- **MinIO 控制台**：访问 `http://服务器IP:9090` (默认账号密码见 .env)
- **Swagger 文档**：访问 `http://服务器IP/api/swagger-ui/index.html`

---

## 🛠️ 五、常用运维命令

| 任务 | 命令 |
| :--- | :--- |
| **查看服务日志** | `docker compose logs -f app` |
| **重启容器** | `docker compose restart` |
| **停止并移除服务** | `docker compose down` |
| **查看资源占用** | `docker stats` |
| **增量更新部署** | `git pull && docker compose up -d --build` |

---

## 🔐 六、安全与优化建议

1. **防火墙配置**：在线上环境，建议仅开放 `80` (HTTP) 和 `22` (SSH) 端口。MySQL(3306)、Redis(6379) 等端口应通过内网访问，非调试需要不建议映射到公网。
2. **内存限制**：本项目的 `docker-compose.yml` 已针对 2G 内存的服务器进行了资源限制配置，若您的服务器配置更高，可适当调大 `limits.memory`。
3. **数据备份**：请定期备份服务器上的 `./mysql/data` 目录。
