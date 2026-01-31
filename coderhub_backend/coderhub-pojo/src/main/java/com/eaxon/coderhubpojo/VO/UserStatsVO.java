package com.eaxon.coderhubpojo.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户统计信息VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsVO {
    /**
     * 文章数
     */
    private Integer articleCount;
    
    /**
     * 关注数（该用户关注了多少人）
     */
    private Integer followingCount;
    
    /**
     * 粉丝数（有多少人关注该用户）
     */
    private Integer followersCount;
    
    /**
     * 当前用户是否已关注该用户（仅在登录状态下有意义）
     */
    private Boolean isFollowing;
}

