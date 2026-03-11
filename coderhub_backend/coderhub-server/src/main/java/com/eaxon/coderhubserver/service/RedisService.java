package com.eaxon.coderhubserver.service;

import java.util.List;
import java.util.Map;

/**
 * Redis服务接口
 * 封装常用的Redis操作
 */
public interface RedisService {
    
    // ==================== 浏览量相关 ====================
    
    /**
     * 增加文章浏览量（原子操作）
     * @param articleId 文章ID
     * @return 增加后的浏览量
     */
    Long incrementViewCount(String articleId);
    
    /**
     * 获取文章浏览量
     * @param articleId 文章ID
     * @return 浏览量，如果不存在返回null
     */
    Long getViewCount(String articleId);
    
    /**
     * 批量获取文章浏览量
     * @param articleIds 文章ID列表
     * @return 文章ID -> 浏览量 的映射
     */
    Map<String, Long> batchGetViewCount(List<String> articleIds);
    
    /**
     * 设置文章浏览量（用于预热）
     * @param articleId 文章ID
     * @param viewCount 浏览量
     */
    void setViewCount(String articleId, Long viewCount);
    
    /**
     * 批量设置文章浏览量（用于预热）
     * @param viewCountMap 文章ID -> 浏览量 的映射
     */
    void batchSetViewCount(Map<String, Long> viewCountMap);
    
    /**
     * 扫描所有浏览量Key
     * @return 所有浏览量的文章ID -> 浏览量映射
     */
    Map<String, Long> scanAllViewCounts();

    // ==================== 通用操作 ====================

    /**
     * 设置值
     * @param key 键
     * @param value 值
     */
    void set(String key, Object value);

    /**
     * 设置值并指定过期时间
     * @param key 键
     * @param value 值
     * @param expireSeconds 过期时间（秒）
     */
    void set(String key, Object value, long expireSeconds);

    /**
     * 获取值
     * @param key 键
     * @return 值
     */
    Object get(String key);

    /**
     * 删除键
     * @param key 键
     */
    void delete(String key);

    /**
     * 递增
     * @param key 键
     * @param delta 增量
     * @return 递增后的值
     */
    Long increment(String key, long delta);

    /**
     * 递减
     * @param key 键
     * @param delta 减量
     * @return 递减后的值
     */
    Long decrement(String key, long delta);

    /**
     * 仅当 key 不存在时才设置值（原子操作，等价于 Redis SETNX）
     * 用于防止并发场景下的覆盖竞争
     * @param key 键
     * @param value 值
     * @return true 表示设置成功（key 原本不存在），false 表示 key 已存在未作修改
     */
    Boolean setIfAbsent(String key, Object value);

    // ==================== AI 提问额度 ====================

    /**
     * 获取用户 AI 提问剩余额度
     * @param userId 用户ID
     * @return 剩余次数，key 不存在时返回 null
     */
    Integer getAiQuota(String userId);

    /**
     * 初始化用户 AI 提问额度（若已存在则不覆盖）
     * @param userId 用户ID
     * @param quota  初始额度
     * @return true=初始化成功，false=已存在未操作
     */
    Boolean initAiQuota(String userId, int quota);

    /**
     * 强制设置用户 AI 提问额度（覆盖已有值）
     * @param userId 用户ID
     * @param quota  额度值
     */
    void setAiQuota(String userId, int quota);

    /**
     * 扣减 AI 提问额度（原子操作，返回扣减后剩余次数）
     * 注意：不检查是否已为 0，调用方需提前判断
     * @param userId 用户ID
     * @return 扣减后剩余次数（可能为负数）
     */
    Long decrementAiQuota(String userId);

    /**
     * 判断用户 AI 额度是否存在（key 是否在 Redis 中）
     * @param userId 用户ID
     * @return true=存在
     */
    Boolean hasAiQuota(String userId);
}
