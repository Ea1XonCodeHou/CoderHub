package com.eaxon.coderhubserver.mapper;

import com.eaxon.coderhubpojo.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息通知 Mapper
 * 
 * @author CoderHub
 * @date 2026-02-03
 */
@Mapper
public interface NotificationMapper {

    /**
     * 插入消息通知
     */
    void insert(Notification notification);

    /**
     * 根据ID查询
     */
    Notification getById(Long id);

    /**
     * 查询用户的未读消息列表
     * @param receiverId 接收者用户ID（UUID字符串）
     * @param limit 返回数量限制
     */
    List<Notification> getUnreadList(@Param("receiverId") String receiverId, 
                                     @Param("limit") Integer limit);

    /**
     * 统计用户未读消息数量
     * @param receiverId 接收者用户ID（UUID字符串）
     */
    Integer countUnread(String receiverId);

    /**
     * 根据ID删除消息
     */
    void deleteById(Long id);

    /**
     * 清空用户所有未读消息
     * @param receiverId 接收者用户ID（UUID字符串）
     */
    void deleteAllUnread(String receiverId);

    /**
     * 删除过期消息（超过7天的未读消息）
     */
    Integer deleteExpired(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 标记消息为已读
     */
    void markAsRead(@Param("id") Long id, @Param("readAt") LocalDateTime readAt);
}
