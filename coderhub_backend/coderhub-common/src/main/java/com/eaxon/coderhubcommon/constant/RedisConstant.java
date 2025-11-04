package com.eaxon.coderhubcommon.constant;

/**
 * Redis Key常量类
 */
public class RedisConstant {
    
    // ==================== 文章相关 ====================
    
    /**
     * 热门文章列表
     * Key: hot:articles
     * Value: List<Article> (JSON)
     * 过期时间: 30分钟
     */
    public static final String HOT_ARTICLES = "hot:articles";
    
    /**
     * 文章详情缓存
     * Key: article:detail:{articleId}
     * Value: Article (JSON)
     * 过期时间: 1小时
     */
    public static final String ARTICLE_DETAIL = "article:detail:";
    
    /**
     * 文章浏览量计数器（异步更新到数据库）
     * Key: article:view:count:{articleId}
     * Value: Long
     * 过期时间: 永久（定时同步到DB）
     */
    public static final String ARTICLE_VIEW_COUNT = "article:view:count:";
    
    /**
     * 文章浏览量计数器前缀（用于SCAN匹配）
     */
    public static final String ARTICLE_VIEW_COUNT_PATTERN = "article:view:count:*";
    
    /**
     * 文章点赞计数（高频操作，减少数据库压力）
     * Key: article:like:count:{articleId}
     * Value: Integer
     * 过期时间: 永久（定时同步到DB）
     */
    public static final String ARTICLE_LIKE_COUNT = "article:like:count:";
    
    /**
     * 用户是否点赞文章（Set集合）
     * Key: article:like:users:{articleId}
     * Value: Set<userId>
     * 过期时间: 1小时
     */
    public static final String ARTICLE_LIKE_USERS = "article:like:users:";
    
    // ==================== 用户相关 ====================
    
    /**
     * 用户Token缓存
     * Key: user:token:{token}
     * Value: UserInfo (JSON)
     * 过期时间: 2小时（与JWT过期时间一致）
     */
    public static final String USER_TOKEN = "user:token:";
    
    /**
     * 用户信息缓存
     * Key: user:info:{userId}
     * Value: User (JSON)
     * 过期时间: 30分钟
     */
    public static final String USER_INFO = "user:info:";
    
    /**
     * 用户统计信息缓存
     * Key: user:stats:{userId}
     * Value: UserStatsVO (JSON)
     * 过期时间: 10分钟
     */
    public static final String USER_STATS = "user:stats:";
    
    // ==================== 分类/标签相关 ====================
    
    /**
     * 分类列表缓存
     * Key: category:list
     * Value: List<Category> (JSON)
     * 过期时间: 1小时
     */
    public static final String CATEGORY_LIST = "category:list";
    
    /**
     * 热门标签列表
     * Key: hot:tags
     * Value: List<Tag> (JSON)
     * 过期时间: 30分钟
     */
    public static final String HOT_TAGS = "hot:tags";
    
    // ==================== 接口限流 ====================
    
    /**
     * 接口访问次数限流
     * Key: rate:limit:{userId}:{api}
     * Value: Integer (访问次数)
     * 过期时间: 1分钟
     */
    public static final String RATE_LIMIT = "rate:limit:";
    
    // ==================== 验证码 ====================
    
    /**
     * 短信/邮箱验证码
     * Key: captcha:{phone/email}
     * Value: String (验证码)
     * 过期时间: 5分钟
     */
    public static final String CAPTCHA = "captcha:";
    
    // ==================== 过期时间（单位：秒） ====================
    
    public static final long EXPIRE_30_MINUTES = 30 * 60;
    public static final long EXPIRE_1_HOUR = 60 * 60;
    public static final long EXPIRE_2_HOURS = 2 * 60 * 60;
    public static final long EXPIRE_1_DAY = 24 * 60 * 60;
    public static final long EXPIRE_5_MINUTES = 5 * 60;
    public static final long EXPIRE_10_MINUTES = 10 * 60;
    public static final long EXPIRE_1_MINUTE = 60;
}

