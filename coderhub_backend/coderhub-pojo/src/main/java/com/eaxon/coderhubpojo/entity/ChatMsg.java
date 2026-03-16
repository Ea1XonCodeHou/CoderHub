package com.eaxon.coderhubpojo.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 私信消息实体
 * 注意：命名为 ChatMsg 以避免与 LangChain4j 的 ChatMessage 类冲突
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMsg {

    private String id;

    private String conversationId;

    private String senderId;

    private String receiverId;

    private String content;

    /** 0=未读，1=已读 */
    private Integer isRead;

    private LocalDateTime createdAt;
}
