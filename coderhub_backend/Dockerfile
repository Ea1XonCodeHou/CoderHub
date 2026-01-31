# 第一阶段：构建 (Build Stage)
# 使用 Maven 镜像作为基础环境
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# 1. 复制父 pom 和子模块 pom 以利用 Docker 缓存
COPY pom.xml .
COPY settings.xml .
COPY coderhub-common/pom.xml coderhub-common/
COPY coderhub-pojo/pom.xml coderhub-pojo/
COPY coderhub-server/pom.xml coderhub-server/

# 下载依赖（使用阿里云镜像加速）
RUN mvn dependency:go-offline -B -s settings.xml

# 2. 复制源码并执行打包
COPY coderhub-common/src coderhub-common/src
COPY coderhub-pojo/src coderhub-pojo/src
COPY coderhub-server/src coderhub-server/src

RUN mvn clean package -DskipTests -s settings.xml

# 第二阶段：运行阶段 (Runtime Stage)
# 使用轻量级 JRE 环境
FROM openjdk:17-jdk-slim
WORKDIR /app

# 设置时区为亚洲/上海
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' > /etc/timezone

# 从构建阶段复制 jar 包
# 这里的 app.jar 对应的是最终生成的可执行文件
COPY --from=build /app/coderhub-server/target/coderhub-server-*.jar app.jar

# 暴露 8080 端口
EXPOSE 8080

# 启动参数说明：
# -Dspring.profiles.active=prod: 激活生产环境配置
# -Xmx512m -Xms256m: 针对核心内存 2G 的机器进行优化，限制 JVM 内存，防止撑爆宿主机
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-Xmx512m", "-Xms256m", "-jar", "app.jar"]
