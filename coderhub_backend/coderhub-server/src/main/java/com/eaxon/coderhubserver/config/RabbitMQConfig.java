package com.eaxon.coderhubserver.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类
 * 声明消息队列、交换机、绑定关系
 * 
 * @author CoderHub
 * @date 2026-02-03
 */
@Configuration
public class RabbitMQConfig {

    // ==================== 交换机名称 ====================
    
    /** 消息通知主题交换机 */
    public static final String NOTIFICATION_EXCHANGE = "notification.topic";
    
    /** 死信交换机 */
    public static final String DEAD_LETTER_EXCHANGE = "notification.dlx";

    // ==================== 队列名称 ====================
    
    /** 社区消息队列 */
    public static final String COMMUNITY_QUEUE = "notification.community";
    
    /** 系统消息队列 */
    public static final String SYSTEM_QUEUE = "notification.system";
    
    /** 死信队列 */
    public static final String DEAD_LETTER_QUEUE = "notification.dlq";

    // ==================== Routing Key ====================
    
    /** 社区消息路由键前缀 */
    public static final String COMMUNITY_ROUTING_KEY = "notification.community.*";
    
    /** 系统消息路由键前缀 */
    public static final String SYSTEM_ROUTING_KEY = "notification.system.*";

    // ==================== 具体路由键 ====================
    
    /** 点赞消息 */
    public static final String ROUTING_KEY_COMMUNITY_LIKE = "notification.community.like";
    
    /** 评论消息 */
    public static final String ROUTING_KEY_COMMUNITY_COMMENT = "notification.community.comment";
    
    /** 关注消息 */
    public static final String ROUTING_KEY_COMMUNITY_FOLLOW = "notification.community.follow";
    
    /** 审核通过消息 */
    public static final String ROUTING_KEY_SYSTEM_AUDIT = "notification.system.audit";
    
    /** 系统警告消息 */
    public static final String ROUTING_KEY_SYSTEM_WARNING = "notification.system.warning";

    // ==================== 交换机定义 ====================

    /**
     * 主题交换机（Topic Exchange）
     * 支持灵活的路由匹配
     */
    @Bean
    public TopicExchange notificationExchange() {
        return ExchangeBuilder
                .topicExchange(NOTIFICATION_EXCHANGE)
                .durable(true)  // 持久化
                .build();
    }

    /**
     * 死信交换机（处理消费失败的消息）
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder
                .directExchange(DEAD_LETTER_EXCHANGE)
                .durable(true)
                .build();
    }

    // ==================== 队列定义 ====================

    /**
     * 社区消息队列
     * 包含：点赞、评论、关注
     */
    @Bean
    public Queue communityQueue() {
        return QueueBuilder
                .durable(COMMUNITY_QUEUE)
                .withArgument("x-message-ttl", 604800000)  // 消息过期时间 7 天（毫秒）
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)  // 绑定死信交换机
                .withArgument("x-dead-letter-routing-key", "notification.dead")
                .build();
    }

    /**
     * 系统消息队列
     * 包含：审核通过、系统警告
     */
    @Bean
    public Queue systemQueue() {
        return QueueBuilder
                .durable(SYSTEM_QUEUE)
                .withArgument("x-message-ttl", 604800000)  // 消息过期时间 7 天
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "notification.dead")
                .build();
    }

    /**
     * 死信队列（用于人工处理失败消息）
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder
                .durable(DEAD_LETTER_QUEUE)
                .build();
    }

    // ==================== 绑定关系 ====================

    /**
     * 绑定社区消息队列到主题交换机
     * 路由键：notification.community.*
     */
    @Bean
    public Binding communityBinding() {
        return BindingBuilder
                .bind(communityQueue())
                .to(notificationExchange())
                .with(COMMUNITY_ROUTING_KEY);
    }

    /**
     * 绑定系统消息队列到主题交换机
     * 路由键：notification.system.*
     */
    @Bean
    public Binding systemBinding() {
        return BindingBuilder
                .bind(systemQueue())
                .to(notificationExchange())
                .with(SYSTEM_ROUTING_KEY);
    }

    /**
     * 绑定死信队列到死信交换机
     */
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("notification.dead");
    }

    // ==================== 消息转换器 ====================

    /**
     * 使用 JSON 序列化消息（解决 Java 序列化安全限制问题）
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置 RabbitTemplate 使用 JSON 序列化
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}

