package com.eaxon.coderhubserver.service.impl;

import com.eaxon.coderhubpojo.DTO.CommentPublishDTO;
import com.eaxon.coderhubpojo.VO.CommentVO;
import com.eaxon.coderhubpojo.entity.ArticleComment;
import com.eaxon.coderhubpojo.entity.User;
import com.eaxon.coderhubserver.mapper.ArticleCommentLikeMapper;
import com.eaxon.coderhubserver.mapper.ArticleCommentMapper;
import com.eaxon.coderhubserver.mapper.ArticleMapper;
import com.eaxon.coderhubserver.mapper.UserMapper;
import com.eaxon.coderhubserver.service.ArticleCommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 文章评论业务实现类
 */
@Service
@Slf4j
public class ArticleCommentServiceImpl implements ArticleCommentService {

    @Autowired
    private ArticleCommentMapper commentMapper;

    @Autowired
    private ArticleCommentLikeMapper commentLikeMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ArticleMapper articleMapper;

    /**
     * 发布评论（包括顶级评论和回复）
     */
    @Override
    @Transactional
    public String publishComment(CommentPublishDTO commentPublishDTO, String userId) {
        log.info("用户{}发布评论：{}", userId, commentPublishDTO);

        // 构建评论对象（createTime 和 updateTime 由 @AutoFill 自动填充）
        ArticleComment comment = ArticleComment.builder()
                .id(UUID.randomUUID().toString())
                .articleId(commentPublishDTO.getArticleId())
                .userId(userId)
                .content(commentPublishDTO.getContent())
                .replyId(commentPublishDTO.getReplyId())
                .likeCount(0)
                .build();

        // 插入评论
        commentMapper.insert(comment);

        // 如果是顶级评论（replyId为空），需要更新article表的comment_count
        if (commentPublishDTO.getReplyId() == null) {
            updateArticleCommentCount(commentPublishDTO.getArticleId());
        }

        log.info("评论发布成功，评论ID：{}", comment.getId());
        return comment.getId();
    }

    /**
     * 删除评论
     */
    @Override
    @Transactional
    public void deleteComment(String commentId, String userId) {
        log.info("用户{}删除评论{}", userId, commentId);

        // 查询评论
        ArticleComment comment = commentMapper.getById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        // 验证是否是评论作者
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除该评论");
        }

        // 删除评论
        commentMapper.deleteById(commentId);

        // 如果是顶级评论，需要更新article表的comment_count
        if (comment.getReplyId() == null) {
            updateArticleCommentCount(comment.getArticleId());
        }

        log.info("评论删除成功");
    }

    /**
     * 获取文章的评论列表（包括顶级评论和回复）
     */
    @Override
    public List<CommentVO> getCommentsByArticleId(String articleId, String userId) {
        log.info("获取文章{}的评论列表", articleId);

        // 1. 查询所有顶级评论
        List<ArticleComment> topComments = commentMapper.getTopCommentsByArticleId(articleId);

        // 2. 构建CommentVO列表
        List<CommentVO> commentVOList = topComments.stream().map(comment -> {
            CommentVO commentVO = buildCommentVO(comment, userId);

            // 3. 查询子评论（回复）
            List<ArticleComment> replies = commentMapper.getRepliesByCommentId(comment.getId());
            List<CommentVO> replyVOList = replies.stream()
                    .map(reply -> buildCommentVO(reply, userId))
                    .collect(Collectors.toList());

            commentVO.setReplies(replyVOList);
            return commentVO;
        }).collect(Collectors.toList());

        log.info("获取到{}条顶级评论", commentVOList.size());
        return commentVOList;
    }

    /**
     * 构建CommentVO对象
     */
    private CommentVO buildCommentVO(ArticleComment comment, String userId) {
        // 查询评论用户信息
        User commentUser = userMapper.getUserById(comment.getUserId());

        // 查询被回复用户信息（如果是回复）
        String replyToUsername = null;
        if (comment.getReplyId() != null) {
            ArticleComment replyComment = commentMapper.getById(comment.getReplyId());
            if (replyComment != null) {
                User replyUser = userMapper.getUserById(replyComment.getUserId());
                replyToUsername = replyUser != null ? replyUser.getUsername() : null;
            }
        }

        // 判断当前用户是否已点赞（如果用户已登录）
        Boolean isLiked = false;
        if (userId != null) {
            isLiked = commentLikeMapper.getByUserIdAndCommentId(userId, comment.getId()) != null;
        }

        return CommentVO.builder()
                .id(comment.getId())
                .articleId(comment.getArticleId())
                .content(comment.getContent())
                .likeCount(comment.getLikeCount())
                .createTime(comment.getCreateTime())
                .userId(comment.getUserId())
                .username(commentUser != null ? commentUser.getUsername() : "未知用户")
                .userAvatar(commentUser != null ? commentUser.getAvatar() : null)
                .replyId(comment.getReplyId())
                .replyToUsername(replyToUsername)
                .isLiked(isLiked)
                .replies(new ArrayList<>())
                .build();
    }

    /**
     * 更新文章的comment_count字段
     */
    private void updateArticleCommentCount(String articleId) {
        Integer count = commentMapper.countByArticleId(articleId);
        articleMapper.updateCommentCount(articleId, count != null ? count : 0);
        log.info("更新文章{}的评论数为{}", articleId, count);
    }
}

