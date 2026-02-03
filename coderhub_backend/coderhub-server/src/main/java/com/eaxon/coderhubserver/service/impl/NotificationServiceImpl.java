package com.eaxon.coderhubserver.service.impl;

import com.eaxon.coderhubpojo.VO.NotificationVO;
import com.eaxon.coderhubpojo.VO.UnreadNotificationVO;
import com.eaxon.coderhubpojo.entity.Notification;
import com.eaxon.coderhubpojo.entity.User;
import com.eaxon.coderhubserver.mapper.NotificationMapper;
import com.eaxon.coderhubserver.mapper.UserMapper;
import com.eaxon.coderhubserver.service.NotificationService;
import com.eaxon.coderhubserver.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息通知服务实现类
 * 
 * @author CoderHub
 * @date 2026-02-03
 */
@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisService redisService;

    private static final String REDIS_UNREAD_KEY_PREFIX = "notification:unread:";
    private static final int MAX_UNREAD_LIST_SIZE = 50;

    @Override
    public UnreadNotificationVO getUnreadNotifications(String userId) {
        log.info("获取用户未读消息: userId={}", userId);

        // 1. 获取未读计数（优先从 Redis）
        Integer unreadCount = getUnreadCount(userId);

        // 2. 查询未读消息列表
        List<Notification> notifications = notificationMapper.getUnreadList(userId, MAX_UNREAD_LIST_SIZE);

        // 3. 转换为 VO（附带触发者信息）
        List<NotificationVO> notificationVOList = notifications.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return UnreadNotificationVO.builder()
                .unreadCount(unreadCount)
                .notifications(notificationVOList)
                .build();
    }

    @Override
    @Transactional
    public void markAsReadAndDelete(Long notificationId, String userId) {
        log.info("标记消息已读并删除: notificationId={}, userId={}", notificationId, userId);

        // 1. 验证消息归属（防止越权）
        Notification notification = notificationMapper.getById(notificationId);
        if (notification == null) {
            log.warn("消息不存在: notificationId={}", notificationId);
            return;
        }

        if (!notification.getReceiverId().equals(userId)) {
            log.warn("无权操作该消息: notificationId={}, userId={}", notificationId, userId);
            throw new RuntimeException("无权操作该消息");
        }

        // 2. 直接删除（已读即删除策略）
        notificationMapper.deleteById(notificationId);

        // 3. Redis 计数 -1
        String redisKey = REDIS_UNREAD_KEY_PREFIX + userId;
        redisService.decrement(redisKey, 1L);

        log.info("消息已删除: notificationId={}", notificationId);
    }

    @Override
    @Transactional
    public void clearAllUnread(String userId) {
        log.info("清空所有未读消息: userId={}", userId);

        // 1. 删除所有未读消息
        notificationMapper.deleteAllUnread(userId);

        // 2. 清空 Redis 计数
        String redisKey = REDIS_UNREAD_KEY_PREFIX + userId;
        redisService.delete(redisKey);

        log.info("所有未读消息已清空: userId={}", userId);
    }

    @Override
    public Integer getUnreadCount(String userId) {
        String redisKey = REDIS_UNREAD_KEY_PREFIX + userId;

        // 1. 尝试从 Redis 获取
        Object countObj = redisService.get(redisKey);
        if (countObj != null) {
            return Integer.parseInt(countObj.toString());
        }

        // 2. Redis 失效，从 MySQL 重建
        Integer count = notificationMapper.countUnread(userId);
        if (count == null) {
            count = 0;
        }

        // 3. 写入 Redis（永久有效，手动维护）
        redisService.set(redisKey, count);

        log.info("从 MySQL 重建未读计数: userId={}, count={}", userId, count);
        return count;
    }

    /**
     * 转换为 VO（附带触发者信息）
     */
    private NotificationVO convertToVO(Notification notification) {
        NotificationVO vo = NotificationVO.builder()
                .id(notification.getId())
                .type(notification.getType())
                .content(notification.getContent())
                .sourceId(notification.getSourceId())
                .sourceType(notification.getSourceType())
                .triggerId(notification.getTriggerId())
                .createdAt(notification.getCreatedAt())
                .build();

        // 如果有触发者ID，查询触发者信息
        if (notification.getTriggerId() != null) {
            User trigger = userMapper.getUserById(notification.getTriggerId());
            if (trigger != null) {
                vo.setTriggerName(trigger.getUsername());
                vo.setTriggerAvatar(trigger.getAvatar());
            }
        }

        return vo;
    }
}
