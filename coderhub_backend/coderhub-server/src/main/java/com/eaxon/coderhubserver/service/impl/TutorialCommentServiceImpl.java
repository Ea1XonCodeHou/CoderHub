package com.eaxon.coderhubserver.service.impl;

import com.eaxon.coderhubpojo.DTO.TutorialCommentPublishDTO;
import com.eaxon.coderhubpojo.VO.TutorialCommentVO;
import com.eaxon.coderhubpojo.entity.TutorialComment;
import com.eaxon.coderhubpojo.entity.User;
import com.eaxon.coderhubserver.mapper.TutorialCommentMapper;
import com.eaxon.coderhubserver.mapper.UserMapper;
import com.eaxon.coderhubserver.service.TutorialCommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 教程评论业务实现类
 */
@Service
@Slf4j
public class TutorialCommentServiceImpl implements TutorialCommentService {

    @Autowired
    private TutorialCommentMapper commentMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 发布评论（包括顶级评论和回复）
     */
    @Override
    @Transactional
    public String publishComment(TutorialCommentPublishDTO dto, String userId) {
        log.info("用户{}在教程{}发布评论", userId, dto.getTutorialId());

        // 构建评论对象
        TutorialComment comment = TutorialComment.builder()
                .id(UUID.randomUUID().toString())
                .tutorialId(dto.getTutorialId())
                .userId(userId)
                .content(dto.getContent())
                .parentId(dto.getParentId())
                .likeCount(0)
                .createTime(LocalDateTime.now())
                .build();

        // 插入评论
        commentMapper.insert(comment);

        log.info("教程评论发布成功，评论ID：{}", comment.getId());
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
        TutorialComment comment = commentMapper.getById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        // 检查权限：只有评论作者才能删除
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此评论");
        }

        // 删除评论（包括其子评论会通过外键CASCADE自动删除）
        commentMapper.deleteById(commentId);

        log.info("评论删除成功");
    }

    /**
     * 获取教程的评论列表（包括顶级评论和回复）
     */
    @Override
    public List<TutorialCommentVO> getCommentsByTutorialId(String tutorialId, String userId) {
        log.info("获取教程{}的评论列表", tutorialId);

        // 获取顶级评论
        List<TutorialComment> topComments = commentMapper.getTopCommentsByTutorialId(tutorialId);

        // 转换为VO并填充用户信息和回复
        List<TutorialCommentVO> result = new ArrayList<>();
        for (TutorialComment comment : topComments) {
            TutorialCommentVO vo = convertToVO(comment, userId);
            
            // 获取子评论（回复）
            List<TutorialComment> replies = commentMapper.getRepliesByCommentId(comment.getId());
            List<TutorialCommentVO> replyVOs = new ArrayList<>();
            for (TutorialComment reply : replies) {
                TutorialCommentVO replyVO = convertToVO(reply, userId);
                
                // 设置被回复用户名（如果有）
                if (reply.getParentId() != null) {
                    TutorialComment parentComment = commentMapper.getById(reply.getParentId());
                    if (parentComment != null) {
                        User parentUser = userMapper.getUserById(parentComment.getUserId());
                        if (parentUser != null) {
                            replyVO.setReplyToUsername(parentUser.getUsername());
                        }
                    }
                }
                
                replyVOs.add(replyVO);
            }
            vo.setReplies(replyVOs);
            
            result.add(vo);
        }

        return result;
    }

    /**
     * 统计教程的评论数
     */
    @Override
    public Integer countByTutorialId(String tutorialId) {
        Integer count = commentMapper.countByTutorialId(tutorialId);
        return count != null ? count : 0;
    }

    /**
     * 将评论实体转换为VO
     */
    private TutorialCommentVO convertToVO(TutorialComment comment, String currentUserId) {
        TutorialCommentVO vo = TutorialCommentVO.builder()
                .id(comment.getId())
                .tutorialId(comment.getTutorialId())
                .content(comment.getContent())
                .likeCount(comment.getLikeCount())
                .createTime(comment.getCreateTime())
                .parentId(comment.getParentId())
                .userId(comment.getUserId())
                .isLiked(false)  // 默认未点赞，后续可扩展点赞功能
                .build();

        // 填充用户信息
        User user = userMapper.getUserById(comment.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setUserAvatar(user.getAvatar());
            vo.setUserLevel(user.getUserLevel());
        }

        // 判断是否是当前用户发布的
        vo.setIsOwner(currentUserId != null && currentUserId.equals(comment.getUserId()));

        return vo;
    }
}

