package com.eaxon.coderhubserver.service.impl;

import com.eaxon.coderhubpojo.DTO.NotificationEvent;
import com.eaxon.coderhubserver.controller.admin.NotificationManagerController.BroadcastRequest;
import com.eaxon.coderhubserver.mq.NotificationProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统通知广播异步服务
 * 独立 Bean，确保 @Async 代理正常生效
 */
@Service
@Slf4j
public class NotificationBroadcastService {

    @Autowired
    private NotificationProducer notificationProducer;

    /**
     * 异步批量向 RabbitMQ 投递系统通知消息
     * 调用方立即返回，本方法在独立线程池中执行
     */
    @Async
    public void asyncBroadcast(List<String> userIds, BroadcastRequest req) {
        String content = buildContent(req);
        int success = 0;
        int fail = 0;

        for (String userId : userIds) {
            try {
                NotificationEvent event = NotificationEvent.builder()
                        .receiverId(userId)
                        .type("SYSTEM_WARNING")
                        .sourceId(null)
                        .sourceType(null)
                        .triggerId(null)
                        .triggerName(null)
                        .extraInfo(content)
                        .createdAt(LocalDateTime.now())
                        .build();

                notificationProducer.sendWarningNotification(event);
                success++;
            } catch (Exception e) {
                fail++;
                log.error("广播通知投递失败，userId={}", userId, e);
            }
        }

        log.info("广播系统通知完成，成功={}, 失败={}", success, fail);
    }

    private String buildContent(BroadcastRequest req) {
        String emoji = (req.getEmoji() != null && !req.getEmoji().isBlank())
                ? req.getEmoji() + " "
                : "";
        return emoji + req.getContent();
    }
}
