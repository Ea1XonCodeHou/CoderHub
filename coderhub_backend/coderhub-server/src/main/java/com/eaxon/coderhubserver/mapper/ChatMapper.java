package com.eaxon.coderhubserver.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.eaxon.coderhubpojo.entity.ChatConversation;
import com.eaxon.coderhubpojo.entity.ChatMsg;

/**
 * 私信聊天 Mapper
 */
@Mapper
public interface ChatMapper {

    // ==================== 会话操作 ====================

    void insertConversation(ChatConversation conversation);

    ChatConversation findConversationByUsers(@Param("user1Id") String user1Id,
                                             @Param("user2Id") String user2Id);

    ChatConversation findConversationById(String id);

    List<ChatConversation> findUserConversations(String userId);

    void updateLastMessage(@Param("id") String id,
                           @Param("lastMessage") String lastMessage,
                           @Param("lastMsgAt") LocalDateTime lastMsgAt);

    // ==================== 消息操作 ====================

    void insertMessage(ChatMsg message);

    List<ChatMsg> findMessages(@Param("conversationId") String conversationId,
                               @Param("offset") int offset,
                               @Param("limit") int limit);

    int countUnread(@Param("receiverId") String receiverId);

    int countUnreadInConversation(@Param("conversationId") String conversationId,
                                  @Param("receiverId") String receiverId);

    void markConversationAsRead(@Param("conversationId") String conversationId,
                                @Param("receiverId") String receiverId);
}
