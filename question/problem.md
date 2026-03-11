# CoderHub 项目问题分析报告

> 生成时间：2026-03-10
> 分析版本：v1.0.4
> 分析范围：前后端全栈（Spring Boot 3.2.5 + Vue 3 + 全套中间件）

---

## 目录

1. [安全漏洞（Critical）](#一安全漏洞critical)
2. [功能性 Bug](#二功能性-bug)
3. [性能问题](#三性能问题)
4. [数据一致性问题](#四数据一致性问题)
5. [代码质量与设计问题](#五代码质量与设计问题)
6. [前端问题](#六前端问题)
7. [部署与配置问题](#七部署与配置问题)
8. [优化建议汇总](#八优化建议汇总)

---

## 一、安全漏洞（Critical）

### 1.1 MD5 密码哈希 — 高危

**文件：** `coderhub-common/src/main/java/com/eaxon/coderhubcommon/utils/MD5Util.java`

**问题描述：**
使用了已被密码学界宣布不安全的 MD5 算法进行密码哈希，且**没有加盐（salt）**。MD5 哈希极易被彩虹表攻击破解。一旦数据库泄露，攻击者可以在分钟内还原大量用户密码。

```java
// 当前实现 - 极不安全
MessageDigest md = MessageDigest.getInstance("MD5");
byte[] bytes = md.digest(source.getBytes(StandardCharsets.UTF_8));
```

**最合理解决方案：** 迁移到 BCrypt，Spring Security 已内置此支持：

```java
// 引入依赖
// implementation 'org.springframework.security:spring-security-crypto'

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtil {
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public static boolean verify(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);  // 内置防时序攻击
    }
}
```

同时需要迁移已有用户密码：在用户下次登录时，先用 MD5 校验通过，然后立刻用 BCrypt 重新加密存储。

---

### 1.2 JWT 密钥填充逻辑弱 — 高危

**文件：** `coderhub-common/src/main/java/com/eaxon/coderhubcommon/utils/JwtUtil.java`

**问题描述：**
`padKey()` 方法通过**重复原始密钥**来达到 32 字节的最低要求，这不会增加任何密码学熵值。如果原始密钥为 `"abc"`，填充后变成 `"abcabcabcabcabcabcabcabcabcabca"`，破解难度等同于原始 3 字节密钥。

```java
// 当前的伪安全逻辑
private static String padKey(String key) {
    if (key.length() >= 32) {
        return key;
    }
    StringBuilder sb = new StringBuilder(key);
    while (sb.length() < 32) {
        sb.append(key);  // 重复密钥毫无意义
    }
    return sb.substring(0, 32);
}
```

**最合理解决方案：** 在 `application.yml` 中直接配置 32 字符以上的强密钥（可用 UUID），删除 `padKey` 方法：

```yaml
# application.yml - 使用足够长的强密钥
coderhub:
  jwt:
    user-secret-key: "u8K#mP2@xL9$nQ5^wR7&yT4!vA6*cE3"  # 至少32字符随机字符串
    admin-secret-key: "jH1%bN8@sD4^fG7!kM3&pW6*qC9#rV2"
```

---

### 1.3 JWT Token 明文写入日志 — 中危

**文件：** 两个 JWT 拦截器

**问题描述：**
拦截器中将完整的 JWT token 打印到日志，运维人员查看日志时会暴露用户身份令牌，可直接用于伪造请求。

```java
log.info("JWT校验开始，token: {}", token);  // 将 token 完整输出
log.info("管理员JWT校验开始，token: {}", token);
```

**最合理解决方案：** 只记录 token 的前几位用于调试追踪，不记录完整 token：

```java
// 安全的日志记录方式
String tokenPreview = token != null && token.length() > 10
    ? token.substring(0, 10) + "..."
    : "null";
log.debug("JWT校验开始，token前缀: {}", tokenPreview);
```

---

### 1.4 ThreadLocal 未在请求完成后清理 — 中危（内存泄漏 + 数据污染）

**文件：** `JwtTokenUserInterceptor.java`、`JwtTokenAdminInterceptor.java`

**问题描述：**
两个拦截器都在 `preHandle` 中将 userId 存入 `BaseContext`（ThreadLocal），但都**没有实现 `afterCompletion` 方法**来清理 ThreadLocal。在 Tomcat 线程池中，线程会被复用，如果上一个请求的数据未清理，下一个请求可能读取到错误的 userId（数据污染）。

```java
// preHandle 中设置了
BaseContext.setCurrentId(userId);

// 但没有 afterCompletion 清理：
// @Override
// public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
//     BaseContext.removeCurrentId();
// }
```

**最合理解决方案：** 在两个拦截器中都实现 `afterCompletion`：

```java
@Override
public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                             Object handler, Exception ex) {
    BaseContext.removeCurrentId();  // 必须清理，防止线程复用时数据污染
}
```

同时确保 `BaseContext` 有对应的 `removeCurrentId()` 方法：

```java
public class BaseContext {
    private static final ThreadLocal<String> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(String id) { threadLocal.set(id); }
    public static String getCurrentId() { return threadLocal.get(); }
    public static void removeCurrentId() { threadLocal.remove(); }  // 必须存在
}
```

---

### 1.5 Swagger UI 在生产环境无认证暴露 — 中危

**文件：** `WebMvcConfiguration.java`、`application.yml`

**问题描述：**
Swagger UI（`/swagger-ui.html`）在生产环境没有任何访问限制，任何人都可以查看所有 API 接口的完整结构、参数和响应格式，为攻击者提供了完整的攻击地图。

**最合理解决方案：** 在 `application-prod.yml` 中禁用 Swagger：

```yaml
# application-prod.yml
springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false
```

或者添加 Spring Security 对 Swagger 路径进行 Basic Auth 保护。

---

### 1.6 登录接口缺少频率限制 — 中危

**问题描述：**
`/user/login` 和 `/admin/login` 接口没有任何频率限制，攻击者可以无限次尝试密码，进行暴力破解攻击。

**最合理解决方案：** 使用 Redis 实现简单的登录频率限制：

```java
// 在 UserServiceImpl.login() 中
String rateLimitKey = "login:attempt:" + loginDTO.getUsername();
Long attempts = redisTemplate.opsForValue().increment(rateLimitKey);
if (attempts == 1) {
    redisTemplate.expire(rateLimitKey, 15, TimeUnit.MINUTES);
}
if (attempts > 5) {
    throw new LoginFailedException("登录失败次数过多，请15分钟后再试");
}
```

---

## 二、功能性 Bug

### 2.1 评论获取异常被静默吞掉 — Bug

**文件：** `coderhub-server/src/main/java/com/eaxon/coderhubserver/controller/user/ArticleController.java`

**问题描述：**
`getCommentsByArticleId` 方法捕获了所有异常并返回空列表，导致数据库错误、网络错误等真实故障被隐藏，前端只看到"暂无评论"而无法感知到服务端错误。

```java
// ArticleController.java 中类似的问题代码
try {
    // 获取评论
} catch (Exception e) {
    log.error("获取评论失败", e);
    return Result.success(new ArrayList<>());  // 吞掉了异常，返回空数据
}
```

**最合理解决方案：** 不要在 Controller 层捕获业务异常，让 `GlobalExceptionHandler` 统一处理：

```java
// 直接调用，让异常冒泡到全局处理器
@GetMapping("/{articleId}/comments")
public Result<List<CommentVO>> getComments(@PathVariable Long articleId) {
    return Result.success(articleCommentService.getComments(articleId));
}
```

---

### 2.2 通知未读数 Redis 与数据库可能不一致 — Bug

**文件：** `NotificationServiceImpl.java`、`NotificationConsumer.java`

**问题描述：**
`NotificationConsumer` 消费 MQ 消息后，先将通知存入数据库，再 `increment` Redis 计数器。如果 Redis 操作失败，计数器不会更新，但数据库中有数据，导致前端显示未读数为 0 但实际有未读通知。同样，`clearAllUnreadNotifications` 删除数据库记录后再更新 Redis，删库成功但 Redis 更新失败也会造成不一致。

**最合理解决方案：** 以数据库为唯一真相来源，Redis 只作缓存：

```java
// 获取未读数时，优先从 Redis 取，Redis 无数据时从 DB 查并回填
public int getUnreadCount(String userId) {
    String key = UNREAD_COUNT_KEY + userId;
    String cached = redisTemplate.opsForValue().get(key);
    if (cached != null) {
        return Integer.parseInt(cached);
    }
    // Redis 无数据，从 DB 查并回填
    int count = notificationMapper.countUnread(userId);
    redisTemplate.opsForValue().set(key, String.valueOf(count), 1, TimeUnit.HOURS);
    return count;
}
```

---

### 2.3 文章浏览量 Redis Key 无过期时间 — Bug（内存泄漏）

**文件：** `RedisServiceImpl.java`

**问题描述：**
文章浏览量存入 Redis 时没有设置过期时间（TTL），已删除的文章的浏览量 key 会永久驻留在 Redis 中，随时间积累导致内存膨胀。

```java
// 当前代码：incrementViewCount() 没有设置 TTL
redisTemplate.opsForValue().increment(key);  // 无过期时间
```

**最合理解决方案：** 初始化 key 时设置一个足够长的过期时间，并在文章删除时清除对应 key：

```java
public void incrementViewCount(Long articleId) {
    String key = ARTICLE_VIEW_PREFIX + articleId;
    Long count = redisTemplate.opsForValue().increment(key);
    if (count != null && count == 1) {
        // 首次写入时设置 30 天过期
        redisTemplate.expire(key, 30, TimeUnit.DAYS);
    }
}
```

---

### 2.4 GlobalExceptionHandler 未处理所有异常类型 — Bug

**文件：** `GlobalExceptionHandler.java`

**问题描述：**
全局异常处理器只处理了 `BaseException` 和 `SQLIntegrityConstraintViolationException`，其他未捕获的运行时异常（如 NullPointerException、数据库连接超时等）会直接返回 Spring 默认的白板错误页或 500 错误，没有统一的错误格式。

**最合理解决方案：** 添加兜底的 Exception 处理：

```java
@ExceptionHandler(Exception.class)
public Result<String> handleException(Exception ex) {
    log.error("未预期的系统错误", ex);
    // 生产环境不暴露详细错误信息
    return Result.error("系统繁忙，请稍后重试");
}

@ExceptionHandler(MethodArgumentNotValidException.class)
public Result<String> handleValidationException(MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult().getFieldErrors().stream()
        .map(FieldError::getDefaultMessage)
        .collect(Collectors.joining("; "));
    return Result.error(message);
}
```

---

## 三、性能问题

### 3.1 前端通知系统采用轮询 — 性能浪费

**文件：** `coderhub_frontend/src/stores/notificationStore.js`

**问题描述：**
通知系统通过每 30 秒轮询 API 来检查新通知。在有 1000 个在线用户的情况下，每分钟会产生约 2000 次 HTTP 请求，大部分都是空请求（没有新通知）。这是对服务器资源的极大浪费，也增加了数据库查询压力。

```javascript
// 每30秒轮询一次
pollingTimer = setInterval(async () => {
    await fetchUnreadCount();
}, 30000);
```

**最合理解决方案：** 使用 SSE（Server-Sent Events）推送通知。后端已经有 SSE 支持（AIController 使用了 SseEmitter），可以复用这套机制：

```java
// 后端：NotificationController 新增 SSE 订阅接口
@GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter subscribe() {
    String userId = BaseContext.getCurrentId();
    SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
    sseEmitterService.addEmitter(userId, emitter);
    return emitter;
}
```

```javascript
// 前端：使用 EventSource 替代轮询
const eventSource = new EventSource('/api/notification/subscribe', {
    headers: { authentication: token }
});
eventSource.onmessage = (event) => {
    const notification = JSON.parse(event.data);
    store.addNotification(notification);
};
```

---

### 3.2 文章列表存在 N+1 查询 — 性能问题

**文件：** `ArticleServiceImpl.java`、对应的 MyBatis XML

**问题描述：**
获取文章列表时，每篇文章的标签（tags）通过单独的查询获取。如果列表有 20 篇文章，就会产生 1 (列表查询) + 20 (标签查询) = 21 次数据库查询。

**最合理解决方案：** 使用 MyBatis 的 `<collection>` 或 JOIN 查询一次性获取文章和标签：

```xml
<!-- 在 ArticleMapper.xml 中 -->
<resultMap id="ArticleWithTagsMap" type="ArticleVO">
    <id property="id" column="id"/>
    <result property="title" column="title"/>
    <!-- 其他字段... -->
    <collection property="tags" ofType="String">
        <result column="tag_name"/>
    </collection>
</resultMap>

<select id="getArticleListWithTags" resultMap="ArticleWithTagsMap">
    SELECT a.*, t.name as tag_name
    FROM article a
    LEFT JOIN article_tag_relation atr ON a.id = atr.article_id
    LEFT JOIN tag t ON atr.tag_id = t.id
    WHERE a.status = 'published'
    ORDER BY a.created_at DESC
</select>
```

---

### 3.3 定时任务批量更新无事务保护 — 性能 + 数据安全

**文件：** `ArticleViewCountSyncTask.java`

**问题描述：**
定时任务扫描 Redis 中所有浏览量 key 并逐一更新 MySQL，如果中途失败（如数据库连接断开），部分文章已更新，部分未更新，且 Redis 中的数据可能已经被清除，导致数据丢失。同时，在集群部署场景下，多个实例同时运行该任务会产生重复更新。

**最合理解决方案：**
1. 使用 Redisson 分布式锁保证单实例执行
2. 批量 UPDATE 替代逐条更新

```java
@Scheduled(cron = "0 0/5 * * * ?")
public void syncViewCounts() {
    // 分布式锁，锁定时间略大于任务执行时间
    RLock lock = redissonClient.getLock("lock:view-count-sync");
    if (!lock.tryLock()) {
        log.info("其他实例正在执行同步任务，本次跳过");
        return;
    }
    try {
        // 批量收集后一次性更新
        doSync();
    } finally {
        lock.unlock();
    }
}
```

---

## 四、数据一致性问题

### 4.1 文章发布时标签操作非原子性

**文件：** `ArticleServiceImpl.java`

**问题描述：**
文章发布流程：① 上传 Markdown 到 OSS → ② 插入文章记录 → ③ 处理标签 → ④ 建立文章-标签关联。虽然步骤 2-4 在 `@Transactional` 内，但步骤 1（OSS 上传）不在事务中。如果 OSS 上传成功但数据库插入失败，OSS 上会留下孤立文件。

**最合理解决方案：** 使用补偿机制：

```java
@Transactional
public void publishArticle(ArticlePublishDTO dto) {
    String ossUrl = null;
    try {
        ossUrl = ossUtil.uploadMarkdown(dto.getContent());
        // 数据库操作...
    } catch (Exception e) {
        // 数据库操作失败，清理 OSS 文件
        if (ossUrl != null) {
            ossUtil.deleteFile(ossUrl);
        }
        throw e;
    }
}
```

---

### 4.2 删除文章时 ChromaDB 向量未保证同步删除

**文件：** `ArticleServiceImpl.java`

**问题描述：**
删除文章时，MySQL 记录被删除（在事务中），但 ChromaDB 向量的删除操作不在事务中。如果 ChromaDB 删除失败，会导致 AI 搜索时仍然检索到已删除文章的内容片段。

**最合理解决方案：** 使用 RabbitMQ 异步确保最终一致性：

```java
// 先删除 MySQL（主数据），再通过 MQ 异步清理 ChromaDB
@Transactional
public void deleteArticle(Long articleId) {
    articleMapper.deleteById(articleId);
    // 发送删除向量的 MQ 消息，消费失败会重试
    notificationProducer.sendDeleteEmbeddingEvent(articleId);
}
```

---

## 五、代码质量与设计问题

### 5.1 两个 JWT 拦截器代码完全重复

**文件：** `JwtTokenUserInterceptor.java`、`JwtTokenAdminInterceptor.java`

**问题描述：**
两个拦截器的逻辑几乎完全相同，区别仅在于使用 `userTokenName`/`adminTokenName` 和 `userSecretKey`/`adminSecretKey`。这违反了 DRY 原则，任何修改都要改两处。

**最合理解决方案：** 抽象基类：

```java
public abstract class BaseJwtInterceptor implements HandlerInterceptor {

    protected abstract String getTokenName(JwtProperties props);
    protected abstract String getSecretKey(JwtProperties props);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) return true;

        String token = request.getHeader(getTokenName(jwtProperties));
        try {
            Claims claims = JwtUtil.parseJWT(getSecretKey(jwtProperties), token);
            BaseContext.setCurrentId(claims.get(JwtClaimsConstant.USER_ID).toString());
            return true;
        } catch (Exception ex) {
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(...) {
        BaseContext.removeCurrentId();
    }
}
```

---

### 5.2 Controller 层缺少输入验证

**问题描述：**
所有 Controller 方法都没有使用 `@Valid` + JSR-303 注解进行入参校验，非法输入（空字符串、超长字段、SQL 注入尝试等）直接传入 Service 层。

**最合理解决方案：** 在 DTO 上添加验证注解，Controller 方法参数加 `@Valid`：

```java
// DTO 添加约束
public class UserRegisterDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度应在2-20字符之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, message = "密码长度不能少于8位")
    private String password;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}

// Controller 加 @Valid
@PostMapping("/register")
public Result<Void> register(@RequestBody @Valid UserRegisterDTO dto) {
    userService.register(dto);
    return Result.success();
}
```

---

### 5.3 AIController 中 AI 配额逻辑与业务逻辑耦合过深

**文件：** `AIController.java`

**问题描述：**
AI 配额检查、扣减、错误响应等逻辑直接写在 Controller 中，代码超过 370 行，Controller 层职责不清晰。且配额相关的魔法数字（如 `0` 表示免费用户）散布在代码中。

**最合理解决方案：** 将配额逻辑封装到 Service 层，使用枚举替代魔法数字：

```java
// 枚举替代魔法数字
public enum UserLevel {
    FREE(0), PREMIUM(1), ADMIN(2);

    private final int value;
    UserLevel(int value) { this.value = value; }
}

// Service 封装配额逻辑
@Service
public class AIQuotaService {
    public boolean checkAndDeductQuota(String userId) {
        // 配额检查和扣减逻辑
    }
}
```

---

## 六、前端问题

### 6.1 AIAssistant.vue 单文件过大（100KB）

**文件：** `coderhub_frontend/src/views/AIAssistant.vue`

**问题描述：**
AIAssistant.vue 文件高达 100KB，是单文件组件过度膨胀的典型案例。该文件包含了流式输出处理、Markdown 渲染、对话管理、工具栏等多个相互独立的功能，维护性极差。

**最合理解决方案：** 拆分为多个子组件：
```
AIAssistant.vue (容器，保留状态管理)
├── ChatSidebar.vue (历史对话列表)
├── ChatMessageList.vue (消息列表渲染)
├── ChatInputBar.vue (输入框和发送按钮)
├── ChatMessage.vue (单条消息含Markdown渲染)
└── ModelSelector.vue (模型选择)
```

---

### 6.2 前端 Token 存储在 localStorage — 安全风险

**文件：** `coderhub_frontend/src/api/user.js`

**问题描述：**
JWT token 存储在 `localStorage` 中，容易被 XSS 攻击读取。如果页面存在 XSS 漏洞，攻击者可以直接窃取用户 token。

**最合理解决方案：** 对于安全性要求较高的场景，使用 `httpOnly Cookie` 存储 token，JS 无法访问。如果必须用 localStorage，需严格防范 XSS（Content Security Policy + DOMPurify 净化用户输入）。

---

### 6.3 API 请求拦截器错误处理不统一

**文件：** `coderhub_frontend/src/api/user.js`、`coderhub_frontend/src/api/notification.js`

**问题描述：**
`notification.js` 对 401 错误的处理与 `user.js` 不一致。`user.js` 会跳转到登录页，而 `notification.js` 只打印警告日志（因为避免轮询时误退出登录）。这表明当前的轮询架构本身就是问题的根源。

**最合理解决方案：** 如果切换到 SSE 推送（如 3.1 所建议），则 notification 的 401 处理应该与其他 API 保持一致。如果保留轮询，应该用计数器限制（连续多次 401 才退出登录）而不是完全忽略。

---

## 七、部署与配置问题

### 7.1 Redis 没有配置密码

**文件：** `application.yml`、`application-prod.yml`

**问题描述：**
Redis 连接没有配置密码，且 Redis 端口（6379）通过 Docker Compose 映射到宿主机，意味着任何能访问服务器 6379 端口的人都可以直接操作 Redis 数据库，包括读取 AI 配额信息、用户未读数、文章浏览量等数据。

**最合理解决方案：**
1. 在 `application.yml` 配置 Redis 密码（通过环境变量注入）
2. 在 `docker-compose.yml` 的 Redis 服务中添加 `--requirepass` 参数
3. 关闭 Redis 的宿主机端口映射（生产环境 Redis 不应暴露）

```yaml
# docker-compose.yml Redis 服务
redis:
  image: redis:7.0-alpine
  command: redis-server --requirepass ${REDIS_PASSWORD}
  # 移除 ports 映射，Redis 只在内部网络访问
  # ports:
  #   - "6379:6379"
```

---

### 7.2 硬编码的 JWT 密钥

**文件：** `application.yml`

**问题描述：**
`application.yml` 中直接写死了 JWT 密钥：
```yaml
user-secret-key: coderHubUser    # 仅11字符，极弱
admin-secret-key: coderHubAdmin  # 仅12字符，极弱
```

这些密钥提交到 Git 后，任何有代码访问权限的人都知道密钥，可以伪造任意用户的 JWT token。

**最合理解决方案：** 通过环境变量注入，不在代码中硬编码：

```yaml
coderhub:
  jwt:
    user-secret-key: ${JWT_USER_SECRET_KEY}
    admin-secret-key: ${JWT_ADMIN_SECRET_KEY}
    user-ttl: 86400000
    admin-ttl: 86400000
```

并在 `.env` 文件（已在 `.gitignore` 中）中设置：
```bash
JWT_USER_SECRET_KEY=your-very-long-random-secret-key-here-at-least-32-chars
JWT_ADMIN_SECRET_KEY=another-very-long-random-secret-key-here-at-least-32-chars
```

---

### 7.3 生产环境数据库连接池未优化

**文件：** `application.yml`

**问题描述：**
Druid 连接池 `max-active` 设置为 8，对于有 AI 对话（长连接）+ 文章浏览 + 管理后台并发的生产环境，连接池容量偏小，高并发时会出现连接等待。

**最合理解决方案：** 根据服务器内存（800MB 容器）和 MySQL 最大连接数适当调整：

```yaml
datasource:
  druid:
    initial-size: 5
    min-idle: 5
    max-active: 20         # 从8提升到20
    max-wait: 60000        # 等待超时60秒
    time-between-eviction-runs-millis: 60000
    min-evictable-idle-time-millis: 300000
    validation-query: SELECT 1
    test-while-idle: true
    test-on-borrow: false
    test-on-return: false
```

---

## 八、优化建议汇总

| 优先级 | 类别 | 问题 | 建议 |
|--------|------|------|------|
| 🔴 紧急 | 安全 | MD5 密码哈希无盐 | 迁移到 BCrypt |
| 🔴 紧急 | 安全 | JWT 密钥硬编码且过弱 | 环境变量注入32+字符强密钥 |
| 🔴 紧急 | 安全 | Token 写入日志 | 只记录 token 前缀 |
| 🟠 重要 | 安全 | ThreadLocal 未清理 | 实现 afterCompletion |
| 🟠 重要 | 安全 | Redis 无密码 | 配置认证 + 禁止外网端口 |
| 🟠 重要 | 安全 | 登录无频率限制 | Redis 计数器限流 |
| 🟠 重要 | Bug | 评论异常被吞掉 | 让全局异常处理器处理 |
| 🟠 重要 | Bug | Redis Key 无 TTL | 设置合理过期时间 |
| 🟡 中等 | 性能 | 通知轮询 | 改为 SSE 推送 |
| 🟡 中等 | 性能 | 文章列表 N+1 查询 | JOIN 查询或批量预加载 |
| 🟡 中等 | 数据 | 定时任务无分布式锁 | 使用 Redisson 锁 |
| 🟡 中等 | 数据 | OSS 与 DB 非原子操作 | 补偿机制清理孤立文件 |
| 🟢 一般 | 质量 | 两个 JWT 拦截器重复 | 抽象基类 |
| 🟢 一般 | 质量 | 缺少输入验证 | 添加 @Valid + JSR-303 |
| 🟢 一般 | 质量 | Swagger 生产环境暴露 | prod 配置禁用 |
| 🟢 一般 | 质量 | AIAssistant.vue 过大 | 拆分子组件 |
| 🟢 一般 | 配置 | 数据库连接池偏小 | max-active 调整为 20 |

---

## 补充说明

以上问题基于静态代码分析。项目整体架构设计合理，技术选型现代，功能完整度高。主要的安全问题（MD5、弱密钥、Token 日志）建议优先修复，其余问题可根据实际情况分批改进。
