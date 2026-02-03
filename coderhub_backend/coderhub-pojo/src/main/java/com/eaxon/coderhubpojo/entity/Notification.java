package com.eaxon.coderhubpojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息通知实体类
 * 
 * @author CoderHub
 * @date 2026-02-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 接收者用户ID（UUID字符串）
     */
    private String receiverId;

    /**
     * 消息类型
     * COMMUNITY_LIKE: 点赞
     * COMMUNITY_COMMENT: 评论
     * COMMUNITY_FOLLOW: 关注
     * SYSTEM_AUDIT: 审核通过
     * SYSTEM_WARNING: 系统警告
     */
    private String type;

    /**
     * 消息内容文案
     */
    private String content;

    /**
     * 来源ID（文章ID等，UUID字符串）
     */
    private String sourceId;

    /**
     * 来源类型：ARTICLE
     */
    private String sourceType;

    /**
     * 触发者用户ID（UUID字符串，社区消息有值，系统消息为NULL）
     */
    private String triggerId;

    /**
     * 是否已读：0-未读，1-已读
     */
    private Integer isRead;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 已读时间
     */
    private LocalDateTime readAt;
}
