package com.eaxon.coderhubpojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户关注关系实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFollow {
    /**
     * 主键ID
     */
    private String id;
    
    /**
     * 关注者ID（谁关注）
     */
    private String followerId;
    
    /**
     * 被关注者ID（被谁关注）
     */
    private String followedId;
    
    /**
     * 关注时间
     */
    private LocalDateTime createTime;
}

