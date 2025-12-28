package com.eaxon.coderhubserver.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.eaxon.coderhubpojo.entity.AIConversation;

/**
 * AI会话Mapper接口
 * 
 * @author CoderHub
 */
@Mapper
public interface AIConversationMapper {

    /**
     * 插入会话
     */
    void insert(AIConversation conversation);

    /**
     * 根据ID查询会话
     */
    AIConversation getById(@Param("id") String id);

    /**
     * 根据用户ID查询会话列表（分页，按更新时间倒序）
     */
    List<AIConversation> getByUserId(@Param("userId") String userId, 
                                      @Param("status") Integer status);

    /**
     * 更新会话
     */
    void update(AIConversation conversation);

    /**
     * 更新会话的消息计数和预览
     */
    void updateMessageInfo(@Param("id") String id, 
                           @Param("messageCount") Integer messageCount,
                           @Param("lastMessagePreview") String lastMessagePreview);

    /**
     * 更新会话标题
     */
    void updateTitle(@Param("id") String id, @Param("title") String title);

    /**
     * 软删除会话
     */
    void softDelete(@Param("id") String id);

    /**
     * 硬删除会话
     */
    void delete(@Param("id") String id);

    /**
     * 统计用户会话数量
     */
    Integer countByUserId(@Param("userId") String userId);
}

