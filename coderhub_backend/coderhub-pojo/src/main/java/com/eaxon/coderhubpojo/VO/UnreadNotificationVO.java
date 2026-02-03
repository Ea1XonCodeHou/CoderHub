package com.eaxon.coderhubpojo.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 未读消息返回对象
 * 
 * @author CoderHub
 * @date 2026-02-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnreadNotificationVO {

    /**
     * 未读消息总数
     */
    private Integer unreadCount;

    /**
     * 未读消息列表（最多返回50条）
     */
    private List<NotificationVO> notifications;
}

