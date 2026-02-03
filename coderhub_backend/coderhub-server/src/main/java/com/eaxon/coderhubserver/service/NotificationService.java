package com.eaxon.coderhubserver.service;

import com.eaxon.coderhubpojo.VO.UnreadNotificationVO;

/**
 * 消息通知服务接口
 * 
 * @author CoderHub
 * @date 2026-02-03
 */
public interface NotificationService {

    /**
     * 获取用户未读消息（含未读计数）
     * @param userId 用户ID（UUID字符串）
     */
    UnreadNotificationVO getUnreadNotifications(String userId);

    /**
     * 标记消息已读并删除
     * @param notificationId 消息ID
     * @param userId 用户ID（UUID字符串）
     */
    void markAsReadAndDelete(Long notificationId, String userId);

    /**
     * 一键清空所有未读消息
     * @param userId 用户ID（UUID字符串）
     */
    void clearAllUnread(String userId);

    /**
     * 获取未读消息数量
     * @param userId 用户ID（UUID字符串）
     */
    Integer getUnreadCount(String userId);
}
