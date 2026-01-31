package com.eaxon.coderhubserver.service.impl;

import com.eaxon.coderhubpojo.entity.ArticleLike;
import com.eaxon.coderhubserver.mapper.ArticleLikeMapper;
import com.eaxon.coderhubserver.mapper.ArticleMapper;
import com.eaxon.coderhubserver.service.ArticleLikeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 文章点赞Service实现类
 */
@Service
@Slf4j
public class ArticleLikeServiceImpl implements ArticleLikeService {

    @Autowired
    private ArticleLikeMapper articleLikeMapper;

    @Autowired
    private ArticleMapper articleMapper;

    /**
     * 点赞/取消点赞（toggle）
     * 同时更新article表的like_count字段
     */
    @Override
    @Transactional
    public Boolean toggleLike(String userId, String articleId) {
        // 检查是否已点赞
        ArticleLike existingLike = articleLikeMapper.getByUserIdAndArticleId(userId, articleId);

        if (existingLike != null) {
            // 已点赞，执行取消点赞
            log.info("用户{}取消点赞文章{}", userId, articleId);
            articleLikeMapper.delete(userId, articleId);
            
            // 同步更新article表的like_count（减1）
            updateArticleLikeCount(articleId);
            
            return false; // 返回false表示已取消点赞
        } else {
            // 未点赞，执行点赞
            log.info("用户{}点赞文章{}", userId, articleId);
            ArticleLike articleLike = ArticleLike.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(userId)
                    .articleId(articleId)
                    .createTime(java.time.LocalDateTime.now())  // 手动设置创建时间
                    .build();
            
            articleLikeMapper.insert(articleLike);
            
            // 同步更新article表的like_count（加1）
            updateArticleLikeCount(articleId);
            
            return true; // 返回true表示已点赞
        }
    }

    /**
     * 获取文章点赞数
     */
    @Override
    public Integer getLikeCount(String articleId) {
        Integer count = articleLikeMapper.countByArticleId(articleId);
        return count != null ? count : 0;
    }

    /**
     * 检查用户是否已点赞
     */
    @Override
    public Boolean isLiked(String userId, String articleId) {
        ArticleLike articleLike = articleLikeMapper.getByUserIdAndArticleId(userId, articleId);
        return articleLike != null;
    }

    /**
     * 同步更新article表的like_count字段
     */
    private void updateArticleLikeCount(String articleId) {
        // 统计实际点赞数
        Integer actualCount = articleLikeMapper.countByArticleId(articleId);
        Integer finalCount = actualCount != null ? actualCount : 0;
        
        // 直接更新article表的like_count字段
        articleMapper.updateLikeCount(articleId, finalCount);
        
        log.info("更新文章{}的点赞数为{}", articleId, finalCount);
    }
}

