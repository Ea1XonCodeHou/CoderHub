package com.eaxon.coderhubserver.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.eaxon.coderhubpojo.entity.AIMessage;

/**
 * AI消息Mapper接口
 * 
 * @author CoderHub
 */
@Mapper
public interface AIMessageMapper {

    /**
     * 插入消息
     */
    void insert(AIMessage message);

    /**
     * 批量插入消息
     */
    void batchInsert(@Param("messages") List<AIMessage> messages);

    /**
     * 根据ID查询消息
     */
    AIMessage getById(@Param("id") String id);

    /**
     * 根据会话ID查询消息列表（按时间升序）
     */
    List<AIMessage> getByConversationId(@Param("conversationId") String conversationId);

    /**
     * 根据会话ID查询最近N条消息（用于上下文）
     */
    List<AIMessage> getRecentMessages(@Param("conversationId") String conversationId,
                                       @Param("limit") Integer limit);

    /**
     * 统计会话消息数量
     */
    Integer countByConversationId(@Param("conversationId") String conversationId);

    /**
     * 获取会话的最后一条消息
     */
    AIMessage getLastMessage(@Param("conversationId") String conversationId);

    /**
     * 删除会话的所有消息
     */
    void deleteByConversationId(@Param("conversationId") String conversationId);

    /**
     * 删除单条消息
     */
    void delete(@Param("id") String id);
}

