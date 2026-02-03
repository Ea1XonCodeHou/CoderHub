package com.eaxon.coderhubserver.controller.user;

import com.eaxon.coderhubcommon.context.BaseContext;
import com.eaxon.coderhubcommon.result.Result;
import com.eaxon.coderhubpojo.VO.UnreadNotificationVO;
import com.eaxon.coderhubserver.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 消息通知控制器（用户端）
 * 
 * @author CoderHub
 * @date 2026-02-03
 */
@RestController
@RequestMapping("/user/notification")
@Tag(name = "消息通知接口")
@Slf4j
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 获取未读消息列表（含未读计数）
     * 前端每 30 秒轮询此接口
     */
    @GetMapping("/unread")
    @Operation(summary = "获取未读消息")
    public Result<UnreadNotificationVO> getUnreadNotifications() {
        String userId = BaseContext.getCurrentId();
        log.info("获取未读消息: userId={}", userId);

        UnreadNotificationVO result = notificationService.getUnreadNotifications(userId);
        return Result.success(result);
    }

    /**
     * 标记消息已读（已读即删除）
     */
    @PostMapping("/{id}/read")
    @Operation(summary = "标记消息已读")
    public Result<Void> markAsRead(@PathVariable Long id) {
        String userId = BaseContext.getCurrentId();
        log.info("标记消息已读: notificationId={}, userId={}", id, userId);

        notificationService.markAsReadAndDelete(id, userId);
        return Result.success();
    }

    /**
     * 一键清空所有未读消息
     */
    @DeleteMapping("/clear")
    @Operation(summary = "清空所有未读消息")
    public Result<Void> clearAll() {
        String userId = BaseContext.getCurrentId();
        log.info("清空所有未读消息: userId={}", userId);

        notificationService.clearAllUnread(userId);
        return Result.success();
    }

    /**
     * 获取未读消息数量（单独接口，可选）
     */
    @GetMapping("/unread-count")
    @Operation(summary = "获取未读消息数量")
    public Result<Integer> getUnreadCount() {
        String userId = BaseContext.getCurrentId();
        Integer count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }
}
