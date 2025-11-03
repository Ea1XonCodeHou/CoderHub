package com.eaxon.coderhubserver.mapper;

import com.eaxon.coderhubpojo.entity.UserFollow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户关注Mapper接口
 */
@Mapper
public interface UserFollowMapper {

    /**
     * 插入关注记录
     */
    void insert(UserFollow userFollow);

    /**
     * 删除关注记录
     */
    void delete(@Param("followerId") String followerId, @Param("followedId") String followedId);

    /**
     * 查询是否已关注
     */
    UserFollow getByFollowerIdAndFollowedId(@Param("followerId") String followerId, @Param("followedId") String followedId);

    /**
     * 统计用户关注数（该用户关注了多少人）
     */
    Integer countFollowingByUserId(String userId);

    /**
     * 统计用户粉丝数（有多少人关注该用户）
     */
    Integer countFollowersByUserId(String userId);
}

