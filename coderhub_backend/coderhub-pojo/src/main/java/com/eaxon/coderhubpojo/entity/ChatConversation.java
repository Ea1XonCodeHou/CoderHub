package com.eaxon.coderhubpojo.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 私信会话实体（两个用户之间唯一）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatConversation {

    /** 会话ID（UUID） */
    private String id;

    /** 参与方1（两个userId中字典序较小者，保证唯一索引） */
    private String user1Id;

    /** 参与方2（两个userId中字典序较大者） */
    private String user2Id;

    /** 最新消息预览（截断到255字符） */
    private String lastMessage;

    /** 最新消息时间 */
    private LocalDateTime lastMsgAt;

    /** 会话创建时间 */
    private LocalDateTime createdAt;
}
