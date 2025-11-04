# 📊 CoderHub项目 Redis使用场景分析

## ✅ 已完成配置
- **Redis依赖**: spring-boot-starter-data-redis
- **配置文件**: application.yml
- **配置类**: RedisConfiguration.java
- **常量类**: RedisConstant.java

---

## 🎯 当前项目可以用Redis做什么？

### 1️⃣ **热门文章缓存** ⭐⭐⭐⭐⭐ （最推荐）

#### **问题**：
- 每次加载首页都查询数据库，性能差
- 热门文章访问频繁，数据库压力大

#### **解决方案**：
```java
// Service层
public List<Article> getHotArticles() {
    // 1. 先从Redis获取
    String key = RedisConstant.HOT_ARTICLES;
    List<Article> articles = (List<Article>) redisTemplate.opsForValue().get(key);
    
    // 2. 如果Redis没有，查询数据库
    if (articles == null) {
        articles = articleMapper.getHotArticles();
        // 3. 存入Redis，30分钟过期
        redisTemplate.opsForValue().set(key, articles, 30, TimeUnit.MINUTES);
    }
    
    return articles;
}
```

#### **收益**：
- ✅ 响应速度从 50ms → 5ms（快10倍）
- ✅ 数据库查询减少 90%+
- ✅ 支持更高并发

---

### 2️⃣ **文章浏览量Redis计数器** ⭐⭐⭐⭐⭐ （强烈推荐）

#### **问题**：
- 每次浏览文章都 UPDATE 数据库，写压力大
- 高并发下可能丢失浏览量

#### **解决方案**：
```java
// Controller层 - 文章详情
@GetMapping("/{id}")
public Result<Article> getArticle(@PathVariable String id) {
    // 1. Redis浏览量+1（极快）
    String key = RedisConstant.ARTICLE_VIEW_COUNT + id;
    redisTemplate.opsForValue().increment(key, 1);
    
    // 2. 返回文章详情
    Article article = articleService.getById(id);
    
    // 3. 从Redis获取最新浏览量
    Long viewCount = (Long) redisTemplate.opsForValue().get(key);
    article.setViewCount(viewCount.intValue());
    
    return Result.success(article);
}

// 定时任务 - 每5分钟同步到数据库
@Scheduled(cron = "0 */5 * * * ?")
public void syncViewCountToDB() {
    // 扫描所有 article:view:count:* 的key
    // 批量更新到数据库
    // 减少数据库写压力
}
```

#### **收益**：
- ✅ 浏览量更新速度极快
- ✅ 数据库写入减少 99%+（5分钟批量一次）
- ✅ 不会因为高并发丢失数据

---

### 3️⃣ **点赞功能优化** ⭐⭐⭐⭐⭐ （强烈推荐）

#### **问题**：
- 点赞/取消点赞频繁操作数据库
- 需要查询用户是否已点赞

#### **解决方案**：
```java
// Service层
public Boolean toggleLike(String userId, String articleId) {
    String countKey = RedisConstant.ARTICLE_LIKE_COUNT + articleId;
    String usersKey = RedisConstant.ARTICLE_LIKE_USERS + articleId;
    
    // 1. 检查用户是否已点赞（Redis Set集合）
    Boolean isMember = redisTemplate.opsForSet().isMember(usersKey, userId);
    
    if (Boolean.TRUE.equals(isMember)) {
        // 取消点赞
        redisTemplate.opsForSet().remove(usersKey, userId);
        redisTemplate.opsForValue().decrement(countKey, 1);
        
        // 异步更新数据库
        articleLikeMapper.delete(userId, articleId);
        
        return false;
    } else {
        // 点赞
        redisTemplate.opsForSet().add(usersKey, userId);
        redisTemplate.opsForValue().increment(countKey, 1);
        
        // 异步更新数据库
        articleLikeMapper.insert(userId, articleId);
        
        return true;
    }
}
```

#### **收益**：
- ✅ 点赞响应极快（毫秒级）
- ✅ 实时显示点赞数
- ✅ 防止重复点赞
- ✅ 数据库压力大幅减少

---

### 4️⃣ **用户Token缓存** ⭐⭐⭐⭐ （推荐）

#### **问题**：
- 每次请求都验证JWT，解析Token
- JWT无法主动失效（退出登录后Token仍有效）

#### **解决方案**：
```java
// 登录成功后
String token = JwtUtil.createJWT(...);
String key = RedisConstant.USER_TOKEN + token;
redisTemplate.opsForValue().set(key, userInfo, 2, TimeUnit.HOURS);

// JWT拦截器中
String token = request.getHeader("authentication");
String key = RedisConstant.USER_TOKEN + token;
UserInfo userInfo = (UserInfo) redisTemplate.opsForValue().get(key);

if (userInfo == null) {
    // Token失效或已退出
    return Result.error("请重新登录");
}

// 退出登录
redisTemplate.delete(RedisConstant.USER_TOKEN + token);
```

#### **收益**：
- ✅ 支持主动退出登录（删除Redis key）
- ✅ 减少JWT解析次数
- ✅ 可以实现单点登录

---

### 5️⃣ **分类/标签列表缓存** ⭐⭐⭐⭐ （推荐）

#### **问题**：
- 分类和标签很少变化，但每次首页加载都查询
- 这些数据非常适合缓存

#### **解决方案**：
```java
// 分类列表
public List<Category> getCategoryList() {
    String key = RedisConstant.CATEGORY_LIST;
    List<Category> categories = (List<Category>) redisTemplate.opsForValue().get(key);
    
    if (categories == null) {
        categories = categoryMapper.list();
        redisTemplate.opsForValue().set(key, categories, 1, TimeUnit.HOURS);
    }
    
    return categories;
}

// 热门标签
public List<Tag> getHotTags() {
    String key = RedisConstant.HOT_TAGS;
    List<Tag> tags = (List<Tag>) redisTemplate.opsForValue().get(key);
    
    if (tags == null) {
        tags = tagMapper.getHotTags();
        redisTemplate.opsForValue().set(key, tags, 30, TimeUnit.MINUTES);
    }
    
    return tags;
}
```

#### **收益**：
- ✅ 首页加载更快
- ✅ 减少数据库查询
- ✅ 数据变化时清除缓存即可

---

### 6️⃣ **接口限流** ⭐⭐⭐⭐ （推荐，防刷必备）

#### **问题**：
- 恶意用户频繁调用接口
- 点赞、评论、关注等接口容易被刷

#### **解决方案**：
```java
// AOP切面实现接口限流
@Aspect
@Component
public class RateLimitAspect {
    
    @Autowired
    private RedisTemplate redisTemplate;
    
    @Around("@annotation(rateLimit)")
    public Object doRateLimit(ProceedingJoinPoint pjp, RateLimit rateLimit) {
        String userId = BaseContext.getCurrentId();
        String api = pjp.getSignature().getName();
        String key = RedisConstant.RATE_LIMIT + userId + ":" + api;
        
        // 获取当前访问次数
        Integer count = (Integer) redisTemplate.opsForValue().get(key);
        
        if (count == null) {
            // 首次访问，设置过期时间
            redisTemplate.opsForValue().set(key, 1, 1, TimeUnit.MINUTES);
        } else if (count >= rateLimit.maxCount()) {
            // 超过限制
            throw new BusinessException("操作过于频繁，请稍后再试");
        } else {
            // 次数+1
            redisTemplate.opsForValue().increment(key, 1);
        }
        
        return pjp.proceed();
    }
}

// 使用方式
@RateLimit(maxCount = 10) // 1分钟最多10次
@PostMapping("/like")
public Result toggleLike(@RequestBody LikeDTO likeDTO) {
    // ...
}
```

#### **收益**：
- ✅ 防止恶意刷接口
- ✅ 保护系统稳定性
- ✅ 提升用户体验

---

### 7️⃣ **验证码存储** ⭐⭐⭐ （推荐）

#### **问题**：
- 短信/邮箱验证码需要临时存储
- 使用数据库存储不合适

#### **解决方案**：
```java
// 发送验证码
String code = RandomUtil.randomNumbers(6);
String key = RedisConstant.CAPTCHA + phone;
redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);

// 验证验证码
String key = RedisConstant.CAPTCHA + phone;
String realCode = (String) redisTemplate.opsForValue().get(key);

if (!code.equals(realCode)) {
    throw new BusinessException("验证码错误");
}

// 验证成功后删除
redisTemplate.delete(key);
```

#### **收益**：
- ✅ 自动过期，无需清理
- ✅ 验证后自动删除
- ✅ 性能好

---

## 📊 Redis使用优先级总结

| 功能 | 优先级 | 实现难度 | 收益 | 建议 |
|------|--------|---------|------|------|
| **热门文章缓存** | ⭐⭐⭐⭐⭐ | ⭐⭐ | 极高 | 立即实现 |
| **浏览量计数器** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | 极高 | 立即实现 |
| **点赞功能优化** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | 极高 | 立即实现 |
| **Token缓存** | ⭐⭐⭐⭐ | ⭐⭐ | 高 | 推荐实现 |
| **分类/标签缓存** | ⭐⭐⭐⭐ | ⭐ | 高 | 推荐实现 |
| **接口限流** | ⭐⭐⭐⭐ | ⭐⭐⭐ | 高 | 推荐实现 |
| **验证码** | ⭐⭐⭐ | ⭐ | 中 | 可选 |

---

## 🚀 建议实现顺序

### **第一阶段（2小时）**：
1. ✅ 热门文章缓存
2. ✅ 分类/标签缓存

### **第二阶段（3小时）**：
1. ✅ 浏览量Redis计数器
2. ✅ 点赞功能优化

### **第三阶段（可选）**：
1. ✅ Token缓存
2. ✅ 接口限流
3. ✅ 验证码

---

## 📝 注意事项

1. **序列化问题**：
   - 复杂对象建议转JSON字符串存储
   - 或使用Jackson序列化器

2. **缓存穿透**：
   - 查询不存在的数据，可以存空值防止穿透

3. **缓存雪崩**：
   - 过期时间加随机值，避免同时过期

4. **缓存一致性**：
   - 数据更新时记得清除对应缓存

5. **Redis持久化**：
   - 开启RDB或AOF持久化，防止数据丢失

---

## 🎯 下一步

告诉我你想先实现哪个功能？我可以立即帮你写代码！

建议从 **热门文章缓存** 或 **浏览量计数器** 开始，这两个最简单且收益最高！

