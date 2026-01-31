# 升级到 Node 20
FROM node:20-alpine as build-stage
WORKDIR /app
# 设置镜像加速
RUN npm config set registry https://registry.npmmirror.com
# 先安装依赖
COPY package*.json ./
RUN npm install
# 复制源码并构建
COPY . .
RUN npm run build

# 运行阶段保持不变
FROM nginx:stable-alpine as production-stage
COPY --from=build-stage /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
