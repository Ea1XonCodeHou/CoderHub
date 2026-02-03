package com.eaxon.coderhubpojo.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 消息通知视图对象（返回给前端）
 * 
 * @author CoderHub
 * @date 2026-02-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationVO {

    /**
     * 消息ID
     */
    private Long id;

    /**
     * 消息类型
     */
    private String type;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 来源ID（文章ID等，UUID字符串）
     */
    private String sourceId;

    /**
     * 来源类型
     */
    private String sourceType;

    /**
     * 触发者用户ID（UUID字符串）
     */
    private String triggerId;

    /**
     * 触发者昵称（用于前端显示）
     */
    private String triggerName;

    /**
     * 触发者头像
     */
    private String triggerAvatar;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}

