package com.eaxon.coderhubserver.service;

/**
 * 用户关注Service接口
 */
public interface UserFollowService {

    /**
     * 关注/取消关注（toggle）
     * @param followerId 关注者ID
     * @param followedId 被关注者ID
     * @return true-已关注，false-已取消关注
     */
    Boolean toggleFollow(String followerId, String followedId);

    /**
     * 检查是否已关注
     * @param followerId 关注者ID
     * @param followedId 被关注者ID
     * @return true-已关注，false-未关注
     */
    Boolean isFollowing(String followerId, String followedId);

    /**
     * 获取用户关注数（该用户关注了多少人）
     * @param userId 用户ID
     * @return 关注数
     */
    Integer getFollowingCount(String userId);

    /**
     * 获取用户粉丝数（有多少人关注该用户）
     * @param userId 用户ID
     * @return 粉丝数
     */
    Integer getFollowersCount(String userId);
}

