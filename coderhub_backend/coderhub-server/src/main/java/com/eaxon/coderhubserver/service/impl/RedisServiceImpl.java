package com.eaxon.coderhubserver.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import com.eaxon.coderhubcommon.constant.RedisConstant;
import com.eaxon.coderhubserver.service.RedisService;

import lombok.extern.slf4j.Slf4j;

/**
 * Redis服务实现类
 */
@Service
@Slf4j
public class RedisServiceImpl implements RedisService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // ==================== 浏览量相关 ====================
    
    @Override
    public Long incrementViewCount(String articleId) {
        try {
            String key = RedisConstant.ARTICLE_VIEW_COUNT + articleId;
            Long newCount = redisTemplate.opsForValue().increment(key, 1L);
            log.debug("文章{}浏览量+1，当前值：{}", articleId, newCount);
            return newCount;
        } catch (Exception e) {
            log.error("Redis增加浏览量失败：articleId={}", articleId, e);
            return null;
        }
    }
    
    @Override
    public Long getViewCount(String articleId) {
        try {
            String key = RedisConstant.ARTICLE_VIEW_COUNT + articleId;
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }
            // 处理不同类型的返回值
            if (value instanceof Integer) {
                return ((Integer) value).longValue();
            } else if (value instanceof Long) {
                return (Long) value;
            } else {
                return Long.parseLong(value.toString());
            }
        } catch (Exception e) {
            log.error("Redis获取浏览量失败：articleId={}", articleId, e);
            return null;
        }
    }
    
    @Override
    public Map<String, Long> batchGetViewCount(List<String> articleIds) {
        Map<String, Long> result = new HashMap<>();
        
        if (articleIds == null || articleIds.isEmpty()) {
            return result;
        }
        
        try {
            // 构建所有的key
            List<String> keys = articleIds.stream()
                    .map(id -> RedisConstant.ARTICLE_VIEW_COUNT + id)
                    .collect(Collectors.toList());
            
            // 批量获取（MGET）
            List<Object> values = redisTemplate.opsForValue().multiGet(keys);
            
            // 组装结果
            for (int i = 0; i < articleIds.size(); i++) {
                String articleId = articleIds.get(i);
                Object value = values != null ? values.get(i) : null;
                
                if (value != null) {
                    Long viewCount;
                    if (value instanceof Integer) {
                        viewCount = ((Integer) value).longValue();
                    } else if (value instanceof Long) {
                        viewCount = (Long) value;
                    } else {
                        viewCount = Long.parseLong(value.toString());
                    }
                    result.put(articleId, viewCount);
                }
            }
            
            log.debug("批量获取浏览量成功，共{}篇文章", result.size());
        } catch (Exception e) {
            log.error("Redis批量获取浏览量失败", e);
        }
        
        return result;
    }
    
    @Override
    public void setViewCount(String articleId, Long viewCount) {
        try {
            String key = RedisConstant.ARTICLE_VIEW_COUNT + articleId;
            // 存储字符串格式的数字，这样可以使用 INCR 命令
            redisTemplate.opsForValue().set(key, String.valueOf(viewCount));
            log.debug("设置文章{}浏览量：{}", articleId, viewCount);
        } catch (Exception e) {
            log.error("Redis设置浏览量失败：articleId={}", articleId, e);
        }
    }
    
    @Override
    public void batchSetViewCount(Map<String, Long> viewCountMap) {
        if (viewCountMap == null || viewCountMap.isEmpty()) {
            return;
        }
        
        try {
            // 使用Pipeline批量设置
            redisTemplate.executePipelined((org.springframework.data.redis.core.RedisCallback<Object>) connection -> {
                viewCountMap.forEach((articleId, viewCount) -> {
                    String key = RedisConstant.ARTICLE_VIEW_COUNT + articleId;
                    // 存储字符串格式的数字
                    redisTemplate.opsForValue().set(key, String.valueOf(viewCount));
                });
                return null;
            });
            
            log.info("批量设置浏览量成功，共{}篇文章", viewCountMap.size());
        } catch (Exception e) {
            log.error("Redis批量设置浏览量失败", e);
        }
    }
    
    @Override
    public Map<String, Long> scanAllViewCounts() {
        Map<String, Long> result = new HashMap<>();
        
        try {
            // 使用SCAN命令遍历（不阻塞Redis）
            ScanOptions options = ScanOptions.scanOptions()
                    .match(RedisConstant.ARTICLE_VIEW_COUNT_PATTERN)
                    .count(100)  // 每次扫描100个key
                    .build();
            
            Cursor<byte[]> cursor = redisTemplate.getConnectionFactory()
                    .getConnection()
                    .scan(options);
            
            while (cursor.hasNext()) {
                String key = new String(cursor.next());
                // 提取articleId：article:view:count:{articleId}
                String articleId = key.replace(RedisConstant.ARTICLE_VIEW_COUNT, "");
                
                // 获取值
                Long viewCount = getViewCount(articleId);
                if (viewCount != null) {
                    result.put(articleId, viewCount);
                }
            }
            
            cursor.close();
            log.info("扫描Redis浏览量完成，共{}篇文章", result.size());
        } catch (Exception e) {
            log.error("Redis扫描浏览量失败", e);
        }
        
        return result;
    }

    // ==================== 通用操作 ====================

    @Override
    public void set(String key, Object value) {
        try {
            // 将值转为 String 存储，避免序列化问题
            redisTemplate.opsForValue().set(key, String.valueOf(value));
            log.debug("Redis SET: key={}", key);
        } catch (Exception e) {
            log.error("Redis SET 失败: key={}", key, e);
        }
    }

    @Override
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Redis GET 失败: key={}", key, e);
            return null;
        }
    }

    @Override
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("Redis DELETE: key={}", key);
        } catch (Exception e) {
            log.error("Redis DELETE 失败: key={}", key, e);
        }
    }

    @Override
    public Long increment(String key, long delta) {
        try {
            Long result = redisTemplate.opsForValue().increment(key, delta);
            log.debug("Redis INCR: key={}, delta={}, result={}", key, delta, result);
            return result;
        } catch (Exception e) {
            log.error("Redis INCR 失败: key={}, delta={}", key, delta, e);
            return null;
        }
    }

    @Override
    public Long decrement(String key, long delta) {
        try {
            Long result = redisTemplate.opsForValue().decrement(key, delta);
            log.debug("Redis DECR: key={}, delta={}, result={}", key, delta, result);
            return result;
        } catch (Exception e) {
            log.error("Redis DECR 失败: key={}, delta={}", key, delta, e);
            return null;
        }
    }
}
