package com.eaxon.coderhubserver.mq;

import com.eaxon.coderhubpojo.DTO.NotificationEvent;
import com.eaxon.coderhubpojo.entity.Notification;
import com.eaxon.coderhubpojo.entity.User;
import com.eaxon.coderhubserver.config.RabbitMQConfig;
import com.eaxon.coderhubserver.mapper.NotificationMapper;
import com.eaxon.coderhubserver.mapper.UserMapper;
import com.eaxon.coderhubserver.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 消息通知消费者
 * 监听 RabbitMQ 队列，消费消息并入库
 * 
 * @author CoderHub
 * @date 2026-02-03
 */
@Component
@Slf4j
public class NotificationConsumer {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisService redisService;

    private static final String REDIS_UNREAD_KEY_PREFIX = "notification:unread:";

    /**
     * 消费社区消息队列
     */
    @RabbitListener(queues = RabbitMQConfig.COMMUNITY_QUEUE)
    public void handleCommunityNotification(NotificationEvent event) {
        try {
            log.info("收到社区消息: type={}, receiverId={}, triggerId={}", 
                    event.getType(), event.getReceiverId(), event.getTriggerId());

            // 1. 构建通知内容
            String content = buildNotificationContent(event);

            // 2. 构建通知实体
            Notification notification = Notification.builder()
                    .receiverId(event.getReceiverId())
                    .type(event.getType())
                    .content(content)
                    .sourceId(event.getSourceId())
                    .sourceType(event.getSourceType())
                    .triggerId(event.getTriggerId())
                    .isRead(0)
                    .createdAt(event.getCreatedAt())
                    .build();

            // 3. 入库
            notificationMapper.insert(notification);

            // 4. 更新 Redis 未读计数
            String redisKey = REDIS_UNREAD_KEY_PREFIX + event.getReceiverId();
            redisService.increment(redisKey, 1L);

            log.info("社区消息处理成功: notificationId={}", notification.getId());

        } catch (Exception e) {
            log.error("消费社区消息失败: {}", event, e);
            throw new RuntimeException("消息处理失败，将重新入队", e);
        }
    }

    /**
     * 消费系统消息队列
     */
    @RabbitListener(queues = RabbitMQConfig.SYSTEM_QUEUE)
    public void handleSystemNotification(NotificationEvent event) {
        try {
            log.info("收到系统消息: type={}, receiverId={}", 
                    event.getType(), event.getReceiverId());

            // 1. 构建通知内容
            String content = buildSystemNotificationContent(event);

            // 2. 构建通知实体
            Notification notification = Notification.builder()
                    .receiverId(event.getReceiverId())
                    .type(event.getType())
                    .content(content)
                    .sourceId(event.getSourceId())
                    .sourceType(event.getSourceType())
                    .triggerId(null)  // 系统消息无触发者
                    .isRead(0)
                    .createdAt(event.getCreatedAt())
                    .build();

            // 3. 入库
            notificationMapper.insert(notification);

            // 4. 更新 Redis 未读计数
            String redisKey = REDIS_UNREAD_KEY_PREFIX + event.getReceiverId();
            redisService.increment(redisKey, 1L);

            log.info("系统消息处理成功: notificationId={}", notification.getId());

        } catch (Exception e) {
            log.error("消费系统消息失败: {}", event, e);
            throw new RuntimeException("消息处理失败，将重新入队", e);
        }
    }

    /**
     * 构建社区消息通知内容
     */
    private String buildNotificationContent(NotificationEvent event) {
        // 优先使用事件中的 triggerName，避免再查询数据库
        String triggerName = event.getTriggerName();
        if (triggerName == null || triggerName.isEmpty()) {
            User trigger = userMapper.getUserById(event.getTriggerId());
            triggerName = trigger != null ? trigger.getUsername() : "某用户";
        }

        String extraInfo = event.getExtraInfo() != null ? event.getExtraInfo() : "";

        return switch (event.getType()) {
            case "COMMUNITY_LIKE" -> triggerName + " 点赞了你的文章" + extraInfo;
            case "COMMUNITY_COMMENT" -> triggerName + " 评论了你的文章" + extraInfo;
            case "COMMUNITY_FOLLOW" -> triggerName + " 关注了你";
            default -> "您有新的消息";
        };
    }

    /**
     * 构建系统消息通知内容
     */
    private String buildSystemNotificationContent(NotificationEvent event) {
        String extraInfo = event.getExtraInfo() != null ? event.getExtraInfo() : "";

        return switch (event.getType()) {
            case "SYSTEM_AUDIT" -> "您的文章" + extraInfo + "已通过审核";
            case "SYSTEM_WARNING" -> "系统提示：" + extraInfo;
            default -> "您有新的系统消息";
        };
    }
}

