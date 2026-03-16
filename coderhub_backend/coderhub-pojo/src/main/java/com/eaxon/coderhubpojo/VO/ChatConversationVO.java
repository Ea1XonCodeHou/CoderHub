package com.eaxon.coderhubpojo.VO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 私信会话列表项 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatConversationVO {

    private String id;

    /** 对方用户ID */
    private String peerId;

    /** 对方昵称 */
    private String peerName;

    /** 对方头像 */
    private String peerAvatar;

    /** 对方会员等级 */
    private Integer peerLevel;

    /** 最新消息预览 */
    private String lastMessage;

    /** 最新消息时间 */
    private LocalDateTime lastMsgAt;

    /** 当前用户在此会话中的未读数 */
    private Integer unreadCount;
}
