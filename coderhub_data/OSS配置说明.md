# 阿里云OSS配置说明 - 实现在线预览

## 📋 问题背景

### 原始问题
- 点击"查看"按钮时，浏览器弹出下载对话框，而非在线预览
- 用户需要先下载文件到本地，才能查看内容
- 与主流在线教育平台的体验不符

### 根本原因
OSS返回文件时，HTTP响应头`Content-Disposition`默认可能为`attachment`，导致浏览器触发下载行为。

---

## 🎯 解决方案对比

| 方案 | 技术实现 | 优势 | 劣势 | 推荐度 |
|------|---------|------|------|--------|
| **方案1: OSS URL参数控制** | 添加`?response-content-disposition=inline` | ✅ 真正在线预览<br>✅ 无需下载<br>✅ 节省带宽<br>✅ 大文件友好 | 需要OSS配置CORS | ⭐⭐⭐⭐⭐ |
| 方案2: PDF.js渲染 | 前端JS库渲染 | ✅ 自定义UI<br>✅ 可加水印 | ❌ 需要完整下载<br>❌ 性能差 | ⭐⭐⭐ |
| 方案3: 后端代理 | 服务器转发 | ✅ 安全性高 | ❌ 占用服务器资源<br>❌ 成本高 | ⭐⭐ |

**结论：方案1是主流在线教育平台的标准做法（腾讯课堂、网易云课堂、B站等）**

---

## 🛠️ 已实现的技术方案

### 前端实现（DocumentViewer.vue）

#### 1. 预览功能（inline模式）
```javascript
// 获取预览URL - 在线显示，不下载
const getPreviewUrl = (url) => {
  if (!url) return ''
  
  if (url.includes('aliyuncs.com')) {
    const separator = url.includes('?') ? '&' : '?'
    // inline: 告诉浏览器在页面内显示，而非下载
    return `${url}${separator}response-content-disposition=inline`
  }
  
  return url
}

// 使用方式
<iframe :src="getPreviewUrl(document.documentUrl)" />
```

**效果：**
- 原始URL: `https://bucket.oss-cn-beijing.aliyuncs.com/tutorial/doc.pdf`
- 预览URL: `https://bucket.oss-cn-beijing.aliyuncs.com/tutorial/doc.pdf?response-content-disposition=inline`
- 浏览器行为：在iframe内直接显示PDF内容

#### 2. 下载功能（attachment模式）
```javascript
// 获取下载URL - 强制下载到本地
const getDownloadUrl = (url, filename) => {
  if (!url) return ''
  
  if (url.includes('aliyuncs.com')) {
    const separator = url.includes('?') ? '&' : '?'
    const encodedFilename = encodeURIComponent(filename || '课件')
    // attachment: 告诉浏览器下载文件
    // filename: 指定下载后的文件名
    return `${url}${separator}response-content-disposition=attachment;filename=${encodedFilename}`
  }
  
  return url
}
```

**效果：**
- 下载URL: `https://bucket.oss-cn-beijing.aliyuncs.com/tutorial/doc.pdf?response-content-disposition=attachment;filename=课件.pdf`
- 浏览器行为：弹出保存对话框，文件名为"课件.pdf"

---

## ⚙️ OSS后台配置（必需）

### 步骤1: 配置CORS（跨域资源共享）

**为什么需要配置CORS？**
- 前端域名（如`http://localhost:3000`）需要访问OSS域名（如`https://xxx.aliyuncs.com`）
- 浏览器的同源策略会阻止跨域请求
- iframe加载PDF需要跨域权限

**配置方法：**

1. 登录阿里云OSS控制台
2. 选择存储桶（Bucket）
3. 找到「权限管理」→「跨域设置（CORS）」
4. 添加以下规则：

```json
{
  "allowedOrigins": [
    "http://localhost:3000",
    "http://localhost:5173",
    "https://你的生产域名.com"
  ],
  "allowedMethods": [
    "GET",
    "HEAD"
  ],
  "allowedHeaders": [
    "*"
  ],
  "exposeHeaders": [
    "ETag",
    "Content-Type",
    "Content-Length",
    "Content-Disposition"
  ],
  "maxAgeSeconds": 3600
}
```

**字段说明：**
- `allowedOrigins`: 允许哪些域名访问（开发环境+生产环境）
- `allowedMethods`: 允许的HTTP方法（GET用于读取文件）
- `allowedHeaders`: 允许的请求头（*表示全部）
- `exposeHeaders`: 暴露给浏览器的响应头（必须包含Content-Disposition）
- `maxAgeSeconds`: 浏览器缓存CORS配置的时间

### 步骤2: 验证OSS URL参数支持

阿里云OSS默认支持以下URL参数：
- `response-content-type`: 设置响应的Content-Type
- `response-content-disposition`: 设置响应的Content-Disposition
- `response-cache-control`: 设置响应的Cache-Control
- `response-expires`: 设置响应的Expires

**测试方法：**
```bash
# 在浏览器中访问：
https://你的bucket.oss-cn-beijing.aliyuncs.com/你的文件.pdf?response-content-disposition=inline

# 如果正常显示PDF，说明配置成功
```

### 步骤3: 配置读取权限（可选）

如果文件需要公开访问：
- Bucket权限：公共读（Public Read）
- 或者：保持私有，使用签名URL（更安全）

---

## 🔐 进阶方案：预签名URL（推荐用于付费课程）

### 为什么需要预签名URL？
- 防止非付费用户直接访问OSS链接
- 限制链接的有效期（如1小时后失效）
- 防止盗链和资源滥用

### 后端实现（Java）

```java
// AliOssUtil.java 添加方法
public String generatePresignedUrl(String objectName, int expireMinutes) {
    OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    
    try {
        // 设置URL过期时间
        Date expiration = new Date(System.currentTimeMillis() + expireMinutes * 60 * 1000);
        
        // 生成预签名URL
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectName);
        request.setExpiration(expiration);
        
        // 设置为inline模式（在线预览）
        ResponseHeaderOverrides responseHeaders = new ResponseHeaderOverrides();
        responseHeaders.setContentDisposition("inline");
        request.setResponseHeaders(responseHeaders);
        
        URL signedUrl = ossClient.generatePresignedUrl(request);
        return signedUrl.toString();
    } finally {
        ossClient.shutdown();
    }
}
```

### API接口示例

```java
// DocumentController.java
@GetMapping("/document/{documentId}/preview-url")
public Result<String> getDocumentPreviewUrl(@PathVariable String documentId) {
    // 1. 验证用户是否有权限访问该文档
    Document document = documentService.getById(documentId);
    if (!hasPermission(document)) {
        return Result.error("无权限访问");
    }
    
    // 2. 从OSS URL提取文件路径
    String objectName = extractObjectName(document.getDocumentUrl());
    
    // 3. 生成有效期1小时的预签名URL
    String presignedUrl = aliOssUtil.generatePresignedUrl(objectName, 60);
    
    return Result.success(presignedUrl);
}
```

### 前端调用

```javascript
// 加载文档时获取临时URL
const loadDocumentPreviewUrl = async (documentId) => {
  const res = await request({
    url: `/document/${documentId}/preview-url`,
    method: 'get'
  })
  
  if (res.code === 1) {
    document.value.previewUrl = res.data
  }
}

// 使用临时URL预览
<iframe :src="document.previewUrl" />
```

**优势：**
- ✅ URL包含签名，无法伪造
- ✅ 自动过期，防止链接被分享
- ✅ 后端可记录每次访问日志
- ✅ 可针对不同用户设置不同权限

---

## 📊 主流平台实现方式对比

| 平台 | 预览方式 | 下载控制 | 安全措施 |
|------|---------|---------|---------|
| **腾讯课堂** | OSS inline参数 | 会员可下载 | 预签名URL + 有效期 |
| **网易云课堂** | OSS inline参数 | 付费课程不可下载 | 预签名URL + 水印 |
| **B站** | 视频切片 + OSS | 不提供下载 | 加密切片 + CDN |
| **慕课网** | OSS inline参数 | 会员可下载 | 预签名URL + 下载次数限制 |

**结论：OSS URL参数控制是行业标准，配合预签名URL实现安全控制**

---

## ✅ 当前项目状态

### 已完成
- ✅ 前端预览/下载URL生成逻辑
- ✅ DocumentViewer.vue使用inline模式预览
- ✅ 下载按钮使用attachment模式下载
- ✅ 文件名编码处理（支持中文）

### 待配置
- ⏳ OSS Bucket CORS规则（需要登录阿里云控制台配置）
- ⏳ 验证URL参数支持（测试访问带参数的OSS URL）

### 可选增强
- 💡 实现预签名URL（用于付费课程保护）
- 💡 添加水印功能（防止截图盗版）
- 💡 下载次数限制（防止滥用）
- 💡 观看时长统计（用户学习数据分析）

---

## 🧪 测试方法

### 1. 测试在线预览
```
1. 进入教程详情页
2. 点击某个文档的"查看"按钮
3. 期望：跳转到DocumentViewer页面，PDF直接显示在左侧iframe中
4. 实际：检查浏览器是否弹出下载框
```

### 2. 测试下载功能
```
1. 在DocumentViewer页面点击"下载"按钮
2. 期望：浏览器弹出保存对话框，文件名为文档标题
3. 实际：文件应该下载到本地，而非在新标签页打开
```

### 3. 检查网络请求
```
打开浏览器开发者工具（F12）→ Network标签
查看iframe加载PDF时的URL，应包含 ?response-content-disposition=inline
```

---

## 🔧 故障排查

### 问题1: 预览时仍然弹出下载框
**原因：**
- OSS CORS未配置
- URL参数未生效
- Bucket权限不足

**解决：**
1. 检查OSS CORS配置
2. 手动访问URL测试：`https://xxx.oss-cn-beijing.aliyuncs.com/file.pdf?response-content-disposition=inline`
3. 查看浏览器Console是否有CORS错误

### 问题2: iframe显示空白
**原因：**
- 跨域被阻止
- PDF文件损坏
- 文件格式不支持

**解决：**
1. 检查Console的CORS错误
2. 单独访问PDF URL验证文件完整性
3. 确认文件格式为标准PDF

### 问题3: 下载文件名乱码
**原因：**
- 文件名未正确编码

**解决：**
```javascript
// 已实现：使用encodeURIComponent编码中文
const encodedFilename = encodeURIComponent(filename || '课件')
```

---

## 📚 相关文档

- [阿里云OSS URL参数说明](https://help.aliyun.com/document_detail/39607.html)
- [阿里云OSS CORS配置](https://help.aliyun.com/document_detail/32016.html)
- [阿里云OSS预签名URL](https://help.aliyun.com/document_detail/32016.html)
- [HTTP Content-Disposition详解](https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Headers/Content-Disposition)

---

## 🎓 总结

### 技术亮点
1. **零下载预览**：用户无需等待下载，即时预览
2. **商业化方案**：采用主流平台的标准实现
3. **灵活控制**：预览/下载分离，体验更好
4. **可扩展性**：为后续预签名URL、水印等功能预留接口

### 业务价值
- 提升用户体验，减少跳出率
- 节省用户流量，特别是移动端
- 支持大文件预览（几百MB的课件也能流畅查看）
- 为付费内容保护打好基础

**现在的实现方式，就是主流在线教育平台的标准做法！** ✨
