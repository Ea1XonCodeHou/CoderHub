# MinIO集成方案 - 视频存储解决方案

## 📋 方案概述

**目标：** 将大文件（视频）存储从阿里云OSS迁移到MinIO自建对象存储

**收益：**
- ✅ 节省阿里云OSS费用（视频流量费很贵）
- ✅ 不限制文件大小（支持2GB+的高清视频）
- ✅ 原画质保存，不压缩
- ✅ 完全掌控数据，可以做加密/水印

---

## 🛠️ 步骤1：安装MinIO服务器

### Windows安装（开发环境）

```powershell
# 下载MinIO
cd D:\
mkdir minio
cd minio
wget https://dl.min.io/server/minio/release/windows-amd64/minio.exe

# 创建数据目录
mkdir data

# 启动MinIO
# 设置用户名密码
$env:MINIO_ROOT_USER="admin"
$env:MINIO_ROOT_PASSWORD="admin123456"

# 启动服务（端口9000为API，9001为控制台）
.\minio.exe server ./data --console-address ":9001"
```

**启动成功后你会看到：**
```
MinIO Object Storage Server
Copyright: 2015-2024 MinIO, Inc.
License: GNU AGPLv3 <https://www.gnu.org/licenses/agpl-3.0.html>
Version: RELEASE.2024-xx-xx

API: http://192.168.1.100:9000
Console: http://192.168.1.100:9001

Documentation: https://min.io/docs/minio/linux/index.html
```

### Docker安装（推荐生产环境）

```bash
docker run -d \
  -p 9000:9000 \
  -p 9001:9001 \
  --name minio \
  -e "MINIO_ROOT_USER=admin" \
  -e "MINIO_ROOT_PASSWORD=admin123456" \
  -v D:/minio/data:/data \
  minio/minio server /data --console-address ":9001"
```

---

## 🎯 步骤2：创建Bucket

1. 打开浏览器访问：`http://localhost:9001`
2. 登录（用户名：admin，密码：admin123456）
3. 点击左侧 **Buckets** → **Create Bucket**
4. 创建以下Buckets：
   - `coderhub-videos`（视频文件）
   - `coderhub-documents`（文档文件）
   - `coderhub-images`（图片文件）
5. 设置访问策略：
   - 选择 `coderhub-videos`
   - 点击 **Access Policy** → 选择 `public`（公开读取）

---

## 💻 步骤3：后端集成MinIO

### 3.1 添加Maven依赖

编辑 `coderhub-backend/coderhub-server/pom.xml`：

```xml
<!-- MinIO客户端 -->
<dependency>
    <groupId>io.minio</groupId>
    <artifactId>minio</artifactId>
    <version>8.5.7</version>
</dependency>
```

### 3.2 配置文件

编辑 `application.yml`：

```yaml
coder:
  # 阿里云OSS配置（保留，用于小文件）
  alioss:
    endpoint: ${coder.alioss.endpoint}
    access-key-id: ${coder.alioss.access-key-id}
    access-key-secret: ${coder.alioss.access-key-secret}
    bucket-name: ${coder.alioss.bucket-name}

  # MinIO配置（新增，用于大文件）
  minio:
    endpoint: http://localhost:9000
    access-key: admin
    secret-key: admin123456
    bucket-name: coderhub-videos
```

编辑 `application-dev.yml`：

```yaml
coder:
  minio:
    endpoint: http://localhost:9000
    access-key: admin
    secret-key: admin123456
    bucket-name: coderhub-videos
```

### 3.3 创建MinIO配置类

创建 `coderhub-common/src/main/java/com/eaxon/coderhubcommon/properties/MinioProperties.java`：

```java
package com.eaxon.coderhubcommon.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "coder.minio")
@Data
public class MinioProperties {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucketName;
}
```

### 3.4 创建MinIO工具类

创建 `coderhub-common/src/main/java/com/eaxon/coderhubcommon/utils/MinioUtil.java`：

```java
package com.eaxon.coderhubcommon.utils;

import io.minio.*;
import io.minio.errors.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Data
@AllArgsConstructor
@Slf4j
public class MinioUtil {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucketName;

    /**
     * 获取MinIO客户端
     */
    private MinioClient getMinioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    /**
     * 上传文件（字节数组）
     *
     * @param bytes      文件字节数组
     * @param objectName 对象名称（如：video/2024/11/07/uuid.mp4）
     * @param contentType 文件类型（如：video/mp4）
     * @return 文件访问URL
     */
    public String upload(byte[] bytes, String objectName, String contentType) {
        MinioClient minioClient = getMinioClient();
        
        try {
            // 检查bucket是否存在
            boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build()
            );
            
            if (!exists) {
                // 创建bucket
                minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(bucketName).build()
                );
                log.info("创建bucket成功：{}", bucketName);
            }
            
            // 上传文件
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                    .contentType(contentType)
                    .build()
            );
            
            // 构建访问URL
            String url = endpoint + "/" + bucketName + "/" + objectName;
            log.info("文件上传成功：{}", url);
            return url;
            
        } catch (Exception e) {
            log.error("MinIO上传失败", e);
            throw new RuntimeException("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 上传文件（InputStream）
     *
     * @param inputStream 输入流
     * @param objectName  对象名称
     * @param contentType 文件类型
     * @param size        文件大小
     * @return 文件访问URL
     */
    public String upload(InputStream inputStream, String objectName, String contentType, long size) {
        MinioClient minioClient = getMinioClient();
        
        try {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build()
            );
            
            String url = endpoint + "/" + bucketName + "/" + objectName;
            log.info("文件上传成功：{}", url);
            return url;
            
        } catch (Exception e) {
            log.error("MinIO上传失败", e);
            throw new RuntimeException("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 删除文件
     *
     * @param objectName 对象名称
     */
    public void delete(String objectName) {
        MinioClient minioClient = getMinioClient();
        
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
            log.info("文件删除成功：{}", objectName);
        } catch (Exception e) {
            log.error("MinIO删除失败", e);
            throw new RuntimeException("文件删除失败：" + e.getMessage());
        }
    }

    /**
     * 获取预签名URL（临时访问URL，用于私有文件）
     *
     * @param objectName 对象名称
     * @param expires    过期时间（秒）
     * @return 预签名URL
     */
    public String getPresignedUrl(String objectName, int expires) {
        MinioClient minioClient = getMinioClient();
        
        try {
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(io.minio.http.Method.GET)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(expires)
                    .build()
            );
        } catch (Exception e) {
            log.error("获取预签名URL失败", e);
            throw new RuntimeException("获取预签名URL失败：" + e.getMessage());
        }
    }
}
```

### 3.5 创建配置Bean

编辑 `coderhub-server/src/main/java/com/eaxon/coderhubserver/config/OssConfiguration.java`：

```java
package com.eaxon.coderhubserver.config;

import com.eaxon.coderhubcommon.properties.AliOssProperties;
import com.eaxon.coderhubcommon.properties.MinioProperties;
import com.eaxon.coderhubcommon.utils.AliOssUtil;
import com.eaxon.coderhubcommon.utils.MinioUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class OssConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public AliOssUtil aliOssUtil(AliOssProperties aliOssProperties){
        log.info("配置阿里云文件上传工具类对象：{}", aliOssProperties);
        return new AliOssUtil(
            aliOssProperties.getEndpoint(), 
            aliOssProperties.getAccessKeyId(),
            aliOssProperties.getAccessKeySecret(), 
            aliOssProperties.getBucketName()
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public MinioUtil minioUtil(MinioProperties minioProperties){
        log.info("配置MinIO文件上传工具类对象：{}", minioProperties);
        return new MinioUtil(
            minioProperties.getEndpoint(),
            minioProperties.getAccessKey(),
            minioProperties.getSecretKey(),
            minioProperties.getBucketName()
        );
    }
}
```

### 3.6 修改文件上传Controller

编辑 `CommonController.java`，添加MinIO上传接口：

```java
@Autowired
private MinioUtil minioUtil;

/**
 * 文件上传接口（MinIO版本，用于大文件）
 */
@PostMapping("/upload/minio")
@ApiOperation(value = "大文件上传接口（MinIO）")
public Result<String> uploadToMinio(MultipartFile file){
    log.info("开始上传大文件到MinIO：{}", file.getOriginalFilename());
    
    try {
        String fileOriginalName = file.getOriginalFilename();
        String extension = fileOriginalName.substring(fileOriginalName.lastIndexOf('.'));
        
        // 生成文件路径：video/2024/11/07/uuid.mp4
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String objectName = "video/" + date + "/" + UUID.randomUUID().toString() + extension;
        
        // 判断文件类型
        String contentType = file.getContentType();
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        
        // 上传到MinIO
        String filePath = minioUtil.upload(
            file.getInputStream(), 
            objectName, 
            contentType, 
            file.getSize()
        );
        
        return Result.success(filePath);
    } catch (IOException e) {
        e.printStackTrace();
        log.error("文件上传失败：{}", e.toString());
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
```

---

## 🎨 步骤4：前端调用MinIO上传

### 4.1 修改上传API

编辑 `coderhub_frontend/src/api/admin.js`：

```javascript
// 上传大文件到MinIO
export function uploadFileToMinio(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/common/upload/minio',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    // 大文件上传超时设置
    timeout: 300000  // 5分钟
  })
}
```

### 4.2 修改视频上传逻辑

编辑 `TutorialDetail.vue` 或相关组件：

```javascript
// 处理视频文件上传
const handleVideoFileUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  try {
    // 判断文件大小，大于10MB使用MinIO
    const fileSize = file.size / 1024 / 1024  // MB
    
    let res
    if (fileSize > 10) {
      // 使用MinIO上传大文件
      console.log('文件大小超过10MB，使用MinIO上传')
      res = await uploadFileToMinio(file)
    } else {
      // 使用OSS上传小文件
      res = await uploadFile(file)
    }
    
    videoForm.videoUrl = res.data
    videoForm.fileSize = file.size
    showMessage('success', '上传成功', '文件已上传')
  } catch (error) {
    console.error('文件上传失败：', error)
    showMessage('error', '上传失败', '文件上传失败，请重试')
  }
}
```

---

## 🎬 步骤5：视频播放器实现

### 5.1 创建VideoViewer.vue

类似DocumentViewer，创建视频播放页面：

```vue
<template>
  <div class="video-viewer">
    <nav class="viewer-navbar">
      <button @click="goBack">返回</button>
      <h3>{{ video?.videoTitle }}</h3>
      <button @click="downloadVideo">下载</button>
    </nav>
    
    <div class="video-container">
      <!-- HTML5视频播放器 -->
      <video 
        ref="videoPlayer"
        :src="video?.videoUrl"
        controls
        controlslist="nodownload"
        class="video-player"
        @loadedmetadata="onVideoLoaded"
        @error="onVideoError"
      >
        您的浏览器不支持视频播放
      </video>
    </div>
    
    <aside class="info-sidebar">
      <!-- 视频信息、教程信息、其他视频 -->
    </aside>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const videoPlayer = ref(null)
const video = ref(null)

// 加载视频信息
const loadVideo = async () => {
  const { videoId } = route.query
  // 从API获取视频信息
  // video.value = ...
}

const onVideoLoaded = () => {
  console.log('视频加载成功')
}

const onVideoError = (e) => {
  console.error('视频加载失败', e)
}

const goBack = () => {
  router.back()
}

const downloadVideo = () => {
  window.open(video.value.videoUrl, '_blank')
}
</script>

<style scoped>
.video-player {
  width: 100%;
  height: 100%;
  background: black;
}
</style>
```

---

## 📊 成本对比

### 阿里云OSS（当前方案）

| 项目 | 费用 |
|------|------|
| 存储费 | ¥0.12/GB/月 |
| 流量费 | ¥0.50/GB（100GB内） |
| 请求费 | ¥0.01/万次 |

**示例：** 100个视频（每个500MB）+ 1000次/月播放
- 存储：50GB × ¥0.12 = ¥6/月
- 流量：500GB × ¥0.50 = ¥250/月
- **总计：¥256/月**

### MinIO自建（新方案）

| 项目 | 费用 |
|------|------|
| 服务器 | ¥100/月（2核4G） |
| 存储 | 硬盘容量（500GB = ¥0） |
| 流量 | 带宽费用（10Mbps = ¥0） |

**总计：¥100/月（固定成本）**

**节省：** 60%以上！

---

## ✅ 混合方案推荐

**最佳实践：**
1. **小文件（<10MB）** → 阿里云OSS
   - 图片（用户头像、封面图）
   - 文档（PDF、Word）
   - Markdown文件

2. **大文件（>10MB）** → MinIO
   - 视频文件
   - 高清图片
   - 大型压缩包

**判断逻辑：**
```javascript
if (fileSize > 10 * 1024 * 1024) {
  // 使用MinIO
  uploadToMinio(file)
} else {
  // 使用OSS
  uploadToOSS(file)
}
```

---

## 🔒 安全配置

### MinIO访问控制

1. **公开Bucket**（适合公开视频）
   ```bash
   # MinIO Console → Buckets → coderhub-videos → Access Policy → public
   ```

2. **私有Bucket + 预签名URL**（适合付费课程）
   ```java
   // 生成1小时有效的访问URL
   String url = minioUtil.getPresignedUrl(objectName, 3600)
   ```

3. **IP白名单**
   ```yaml
   # MinIO配置文件
   MINIO_API_CORS_ALLOW_ORIGIN: "http://localhost:3000"
   ```

---

## 🚀 生产环境部署

### 使用Docker Compose

创建 `docker-compose.yml`：

```yaml
version: '3.8'

services:
  minio:
    image: minio/minio
    container_name: minio
    ports:
      - "9000:9000"
      - "9001:9001"
    environment:
      MINIO_ROOT_USER: admin
      MINIO_ROOT_PASSWORD: admin123456
    volumes:
      - ./data:/data
    command: server /data --console-address ":9001"
    restart: always
```

启动：
```bash
docker-compose up -d
```

---

## 📝 总结

### 立即解决方案（推荐先用）
修改 `application.yml`：
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 500MB
      max-request-size: 500MB
```
**重启后端即可！**

### 长期方案（推荐迁移）
按照上面步骤配置MinIO：
1. 安装MinIO（10分钟）
2. 添加依赖+配置（10分钟）
3. 修改上传逻辑（5分钟）
4. 测试（5分钟）

**总耗时：30分钟，节省60%成本！**

---

**你想先快速解决（改配置），还是直接上MinIO？我都可以帮你配置！** 🚀
