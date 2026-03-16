package com.eaxon.coderhubserver.service;

import java.util.List;

import com.eaxon.coderhubpojo.VO.ChatConversationVO;
import com.eaxon.coderhubpojo.VO.ChatMsgVO;

/**
 * 私信聊天 Service
 */
public interface ChatService {

    /**
     * 获取当前用户的会话列表（含系统欢迎会话）
     */
    List<ChatConversationVO> getConversations(String currentUserId);

    /**
     * 获取或创建两个用户之间的会话，返回会话ID
     */
    String getOrCreateConversationId(String currentUserId, String targetUserId);

    /**
     * 加载会话中的历史消息（按时间升序，分页）
     */
    List<ChatMsgVO> getMessages(String conversationId, String currentUserId, int page, int pageSize);

    /**
     * 标记会话中所有消息为已读，并更新 Redis 计数
     */
    void markAsRead(String conversationId, String currentUserId);

    /**
     * 获取当前用户私信未读总数
     */
    int getUnreadCount(String userId);

    /**
     * 内部：保存消息并返回 VO（供 WebSocket Handler 调用）
     */
    ChatMsgVO saveAndPushMessage(String senderId, String receiverId, String content);
}
