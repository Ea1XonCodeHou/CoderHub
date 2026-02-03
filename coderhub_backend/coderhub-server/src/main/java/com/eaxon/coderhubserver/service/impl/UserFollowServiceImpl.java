package com.eaxon.coderhubserver.service.impl;

import com.eaxon.coderhubpojo.DTO.NotificationEvent;
import com.eaxon.coderhubpojo.entity.User;
import com.eaxon.coderhubpojo.entity.UserFollow;
import com.eaxon.coderhubserver.mapper.UserFollowMapper;
import com.eaxon.coderhubserver.mapper.UserMapper;
import com.eaxon.coderhubserver.mq.NotificationProducer;
import com.eaxon.coderhubserver.service.UserFollowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 用户关注Service实现类
 */
@Service
@Slf4j
public class UserFollowServiceImpl implements UserFollowService {

    @Autowired
    private UserFollowMapper userFollowMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationProducer notificationProducer;

    /**
     * 关注/取消关注（toggle）
     */
    @Override
    @Transactional
    public Boolean toggleFollow(String followerId, String followedId) {
        // 不能关注自己
        if (followerId.equals(followedId)) {
            throw new IllegalArgumentException("不能关注自己");
        }

        // 检查是否已关注
        UserFollow existingFollow = userFollowMapper.getByFollowerIdAndFollowedId(followerId, followedId);

        if (existingFollow != null) {
            // 已关注，执行取消关注
            log.info("用户{}取消关注用户{}", followerId, followedId);
            userFollowMapper.delete(followerId, followedId);
            return false; // 返回false表示已取消关注
        } else {
            // 未关注，执行关注
            log.info("用户{}关注用户{}", followerId, followedId);
            UserFollow userFollow = UserFollow.builder()
                    .id(UUID.randomUUID().toString())
                    .followerId(followerId)
                    .followedId(followedId)
                    .createTime(LocalDateTime.now())
                    .build();

            userFollowMapper.insert(userFollow);

            // 【消息通知】发送关注消息
            sendFollowNotification(followerId, followedId);

            return true; // 返回true表示已关注
        }
    }

    /**
     * 检查是否已关注
     */
    @Override
    public Boolean isFollowing(String followerId, String followedId) {
        UserFollow userFollow = userFollowMapper.getByFollowerIdAndFollowedId(followerId, followedId);
        return userFollow != null;
    }

    /**
     * 获取用户关注数（该用户关注了多少人）
     */
    @Override
    public Integer getFollowingCount(String userId) {
        Integer count = userFollowMapper.countFollowingByUserId(userId);
        return count != null ? count : 0;
    }

    /**
     * 获取用户粉丝数（有多少人关注该用户）
     */
    @Override
    public Integer getFollowersCount(String userId) {
        Integer count = userFollowMapper.countFollowersByUserId(userId);
        return count != null ? count : 0;
    }

    /**
     * 发送关注通知消息
     */
    private void sendFollowNotification(String followerId, String followedId) {
        try {
            // 1. 查询关注者信息
            User follower = userMapper.getUserById(followerId);
            String followerName = follower != null ? follower.getUsername() : "某用户";

            // 2. 构建消息事件（userId 是 UUID 字符串）
            NotificationEvent event = NotificationEvent.builder()
                    .receiverId(followedId)  // 被关注者
                    .type("COMMUNITY_FOLLOW")
                    .sourceId(null)  // 关注事件无来源
                    .sourceType(null)
                    .triggerId(followerId)  // 关注者
                    .triggerName(followerName)
                    .createdAt(LocalDateTime.now())
                    .build();

            // 3. 发送到 RabbitMQ
            notificationProducer.sendFollowNotification(event);

        } catch (Exception e) {
            log.error("发送关注通知失败: followerId={}, followedId={}", followerId, followedId, e);
            // 不影响主流程，仅记录日志
        }
    }
}

