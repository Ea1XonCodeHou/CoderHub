package com.eaxon.coderhubpojo.VO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 私信消息 VO（用于历史记录和 WebSocket 推送）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMsgVO {

    private String id;

    private String conversationId;

    private String senderId;

    private String senderName;

    private String senderAvatar;

    private String receiverId;

    private String content;

    private Integer isRead;

    private LocalDateTime createdAt;

    /** 是否是当前用户发送的（供前端判断气泡方向） */
    private Boolean isMine;
}
