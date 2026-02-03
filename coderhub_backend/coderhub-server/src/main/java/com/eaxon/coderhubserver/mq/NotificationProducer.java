package com.eaxon.coderhubserver.mq;

import com.eaxon.coderhubpojo.DTO.NotificationEvent;
import com.eaxon.coderhubserver.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 消息通知生产者
 * 统一发送消息到 RabbitMQ
 * 
 * @author CoderHub
 * @date 2026-02-03
 */
@Component
@Slf4j
public class NotificationProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送点赞消息
     */
    public void sendLikeNotification(NotificationEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.ROUTING_KEY_COMMUNITY_LIKE,
                    event
            );
            log.info("发送点赞消息成功: receiverId={}, triggerId={}", 
                    event.getReceiverId(), event.getTriggerId());
        } catch (Exception e) {
            log.error("发送点赞消息失败: {}", event, e);
        }
    }

    /**
     * 发送评论消息
     */
    public void sendCommentNotification(NotificationEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.ROUTING_KEY_COMMUNITY_COMMENT,
                    event
            );
            log.info("发送评论消息成功: receiverId={}, triggerId={}", 
                    event.getReceiverId(), event.getTriggerId());
        } catch (Exception e) {
            log.error("发送评论消息失败: {}", event, e);
        }
    }

    /**
     * 发送关注消息
     */
    public void sendFollowNotification(NotificationEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.ROUTING_KEY_COMMUNITY_FOLLOW,
                    event
            );
            log.info("发送关注消息成功: receiverId={}, triggerId={}", 
                    event.getReceiverId(), event.getTriggerId());
        } catch (Exception e) {
            log.error("发送关注消息失败: {}", event, e);
        }
    }

    /**
     * 发送审核通过消息
     */
    public void sendAuditNotification(NotificationEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.ROUTING_KEY_SYSTEM_AUDIT,
                    event
            );
            log.info("发送审核通过消息成功: receiverId={}", event.getReceiverId());
        } catch (Exception e) {
            log.error("发送审核通过消息失败: {}", event, e);
        }
    }

    /**
     * 发送系统警告消息
     */
    public void sendWarningNotification(NotificationEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.ROUTING_KEY_SYSTEM_WARNING,
                    event
            );
            log.info("发送系统警告消息成功: receiverId={}", event.getReceiverId());
        } catch (Exception e) {
            log.error("发送系统警告消息失败: {}", event, e);
        }
    }
}

