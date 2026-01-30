package com.eaxon.coderhubpojo.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 关注用户列表展示VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFollowVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String username;
    private String avatar;
    private String bio;          // 个人简介（如有）
    private Integer articleCount; // 文章数
    private Integer followersCount; // 粉丝数
    private Boolean isFollowing;  // 当前用户是否已关注
}

