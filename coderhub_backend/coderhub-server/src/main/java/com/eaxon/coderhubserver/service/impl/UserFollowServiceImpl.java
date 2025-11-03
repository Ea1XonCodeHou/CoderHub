package com.eaxon.coderhubserver.service.impl;

import com.eaxon.coderhubpojo.entity.UserFollow;
import com.eaxon.coderhubserver.mapper.UserFollowMapper;
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
}

