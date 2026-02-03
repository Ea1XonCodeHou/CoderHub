package com.eaxon.coderhubpojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息通知事件（用于 RabbitMQ 传输）
 * 
 * @author CoderHub
 * @date 2026-02-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 接收者用户ID（UUID字符串）
     */
    private String receiverId;

    /**
     * 消息类型
     */
    private String type;

    /**
     * 来源ID（文章ID等，UUID字符串）
     */
    private String sourceId;

    /**
     * 来源类型：ARTICLE
     */
    private String sourceType;

    /**
     * 触发者用户ID（UUID字符串，点赞/评论/关注的人）
     */
    private String triggerId;

    /**
     * 触发者昵称（可选，避免Consumer再查询）
     */
    private String triggerName;

    /**
     * 事件创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 额外信息（可选，如文章标题）
     */
    private String extraInfo;
}
