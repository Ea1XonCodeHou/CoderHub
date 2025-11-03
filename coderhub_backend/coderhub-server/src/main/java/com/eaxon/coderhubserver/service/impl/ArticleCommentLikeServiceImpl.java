package com.eaxon.coderhubserver.service.impl;

import com.eaxon.coderhubpojo.entity.ArticleCommentLike;
import com.eaxon.coderhubserver.mapper.ArticleCommentLikeMapper;
import com.eaxon.coderhubserver.mapper.ArticleCommentMapper;
import com.eaxon.coderhubserver.service.ArticleCommentLikeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文章评论点赞业务实现类
 */
@Service
@Slf4j
public class ArticleCommentLikeServiceImpl implements ArticleCommentLikeService {

    @Autowired
    private ArticleCommentLikeMapper commentLikeMapper;

    @Autowired
    private ArticleCommentMapper commentMapper;

    /**
     * 点赞/取消点赞评论
     */
    @Override
    @Transactional
    public Map<String, Object> toggleLike(String userId, String commentId) {
        ArticleCommentLike existingLike = commentLikeMapper.getByUserIdAndCommentId(userId, commentId);

        boolean liked;
        if (existingLike != null) {
            // 已点赞，执行取消点赞
            log.info("用户{}取消点赞评论{}", userId, commentId);
            commentLikeMapper.delete(userId, commentId);
            liked = false;
        } else {
            // 未点赞，执行点赞
            log.info("用户{}点赞评论{}", userId, commentId);
            ArticleCommentLike commentLike = ArticleCommentLike.builder()
                    .id(UUID.randomUUID().toString())
                    .commentId(commentId)
                    .userId(userId)
                    .createTime(LocalDateTime.now())
                    .build();

            commentLikeMapper.insert(commentLike);
            liked = true;
        }

        // 同步更新comment表的like_count
        Integer likeCount = commentLikeMapper.countByCommentId(commentId);
        commentMapper.updateLikeCount(commentId, likeCount != null ? likeCount : 0);

        // 构造返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("likeCount", likeCount != null ? likeCount : 0);

        return result;
    }

    /**
     * 检查用户是否已点赞评论
     */
    @Override
    public Boolean isLiked(String userId, String commentId) {
        return commentLikeMapper.getByUserIdAndCommentId(userId, commentId) != null;
    }
}

