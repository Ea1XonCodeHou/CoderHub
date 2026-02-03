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
}
