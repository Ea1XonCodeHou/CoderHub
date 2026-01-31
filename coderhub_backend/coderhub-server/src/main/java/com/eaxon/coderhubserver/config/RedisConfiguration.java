package com.eaxon.coderhubserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import lombok.extern.slf4j.Slf4j;

/**
 * Redis配置类
 */
@Configuration
@Slf4j
public class RedisConfiguration {
    
    /**
     * 创建RedisTemplate Bean
     * 设置Key和Value都使用String序列化器，确保Redis中的数据都是字符串格式
     * 这样可以直接使用 INCR 等命令操作数字
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.info("开始创建Redis模板对象");
        
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        
        // 设置连接工厂对象
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        
        // 使用String序列化器
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        
        // 设置Key的序列化器
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        
        // 设置Value的序列化器（关键！）
        // 使用String序列化器，这样存储的数字就是纯字符串 "123"，可以执行 INCR
        redisTemplate.setValueSerializer(stringRedisSerializer);
        redisTemplate.setHashValueSerializer(stringRedisSerializer);
        
        // 初始化RedisTemplate
        redisTemplate.afterPropertiesSet();
        
        log.info("Redis模板对象创建成功（使用String序列化器）");
        return redisTemplate;
    }
}

