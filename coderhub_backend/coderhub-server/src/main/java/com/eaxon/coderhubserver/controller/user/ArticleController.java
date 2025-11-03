package com.eaxon.coderhubserver.controller.user;

import com.eaxon.coderhubcommon.context.BaseContext;
import com.eaxon.coderhubcommon.result.Result;
import com.eaxon.coderhubpojo.DTO.ArticlePublishDTO;
import com.eaxon.coderhubpojo.VO.ArticleDetailVO;
import com.eaxon.coderhubpojo.VO.ArticleVO;
import com.eaxon.coderhubpojo.entity.Tag;
import com.eaxon.coderhubserver.service.ArticleService;
import com.eaxon.coderhubserver.service.ArticleLikeService;
import com.eaxon.coderhubserver.service.ArticleCommentService;
import com.eaxon.coderhubserver.service.ArticleCommentLikeService;
import com.eaxon.coderhubserver.mapper.TagMapper;
import com.eaxon.coderhubpojo.DTO.CommentPublishDTO;
import com.eaxon.coderhubpojo.VO.CommentVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文章控制器（用户端）
 */
@RestController
@RequestMapping("/article")
@Slf4j
@Api(tags = "文章相关接口")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleLikeService articleLikeService;

    @Autowired
    private ArticleCommentService commentService;

    @Autowired
    private ArticleCommentLikeService commentLikeService;

    @Autowired
    private TagMapper tagMapper;

    /**
     * 发布文章
     */
    @PostMapping("/publish")
    @ApiOperation("发布文章")
    public Result<ArticleVO> publishArticle(@RequestBody ArticlePublishDTO dto) {
        log.info("发布文章：{}", dto.getTitle());
        ArticleVO vo = articleService.publishArticle(dto);
        return Result.success(vo);
    }

    /**
     * 获取文章详情
     */
    @GetMapping("/{articleId}")
    @ApiOperation("获取文章详情")
    public Result<ArticleDetailVO> getArticleDetail(@PathVariable String articleId) {
        log.info("获取文章详情：articleId={}", articleId);
        ArticleDetailVO vo = articleService.getArticleDetail(articleId);
        return Result.success(vo);
    }

    /**
     * 获取文章列表
     */
    @GetMapping("/list")
    @ApiOperation("获取文章列表")
    public Result<List<ArticleVO>> getArticleList(
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String tagId,
            @RequestParam(required = false) Integer status
    ) {
        log.info("获取文章列表：categoryId={}, tagId={}, status={}", categoryId, tagId, status);
        List<ArticleVO> list = articleService.getArticleList(categoryId, tagId, status);
        return Result.success(list);
    }

    /**
     * 删除文章
     */
    @DeleteMapping("/{articleId}")
    @ApiOperation("删除文章")
    public Result<String> deleteArticle(@PathVariable String articleId) {
        log.info("删除文章：articleId={}", articleId);
        articleService.deleteArticle(articleId);
        return Result.success("删除成功");
    }

    /**
     * 搜索标签（用于自动完成）
     */
    @GetMapping("/tags/search")
    @ApiOperation("搜索标签")
    public Result<List<Tag>> searchTags(@RequestParam String keyword) {
        log.info("搜索标签：keyword={}", keyword);
        List<Tag> tags = tagMapper.searchByKeyword(keyword);
        return Result.success(tags);
    }

    /**
     * 获取热门标签
     */
    @GetMapping("/tags/hot")
    @ApiOperation("获取热门标签")
    public Result<List<Tag>> getHotTags(@RequestParam(defaultValue = "20") Integer limit) {
        log.info("获取热门标签：limit={}", limit);
        List<Tag> tags = tagMapper.getHotTags(limit);
        return Result.success(tags);
    }

    /**
     * 点赞/取消点赞文章（toggle）
     */
    @PostMapping("/{articleId}/like")
    @ApiOperation("点赞/取消点赞文章")
    public Result<Map<String, Object>> toggleLike(@PathVariable String articleId) {
        // 从ThreadLocal获取当前登录用户ID
        String userId = BaseContext.getCurrentId();
        log.info("用户{}操作文章{}的点赞", userId, articleId);
        
        // 执行点赞/取消点赞
        Boolean liked = articleLikeService.toggleLike(userId, articleId);
        
        // 获取最新的点赞数
        Integer likeCount = articleLikeService.getLikeCount(articleId);
        
        // 构造返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("likeCount", likeCount);
        
        return Result.success(result);
    }

    /**
     * 获取文章点赞数
     */
    @GetMapping("/{articleId}/like/count")
    @ApiOperation("获取文章点赞数")
    public Result<Integer> getLikeCount(@PathVariable String articleId) {
        log.info("获取文章{}的点赞数", articleId);
        Integer count = articleLikeService.getLikeCount(articleId);
        return Result.success(count);
    }

    /**
     * 检查用户是否已点赞文章
     */
    @GetMapping("/{articleId}/like/status")
    @ApiOperation("检查用户是否已点赞文章")
    public Result<Boolean> checkLikeStatus(@PathVariable String articleId) {
        String userId = BaseContext.getCurrentId();
        log.info("检查用户{}是否点赞文章{}", userId, articleId);
        Boolean liked = articleLikeService.isLiked(userId, articleId);
        return Result.success(liked);
    }

    // ==================== 评论相关接口 ====================

    /**
     * 发布评论（包括顶级评论和回复）
     */
    @PostMapping("/{articleId}/comment")
    @ApiOperation("发布评论")
    public Result<String> publishComment(
            @PathVariable String articleId,
            @RequestBody CommentPublishDTO commentPublishDTO) {
        // 从ThreadLocal获取当前登录用户ID
        String userId = BaseContext.getCurrentId();
        log.info("用户{}在文章{}发布评论", userId, articleId);

        // 确保DTO中的articleId与路径参数一致
        commentPublishDTO.setArticleId(articleId);
        String commentId = commentService.publishComment(commentPublishDTO, userId);
        return Result.success(commentId);
    }

    /**
     * 获取文章的评论列表
     */
    @GetMapping("/{articleId}/comment")
    @ApiOperation("获取文章评论列表")
    public Result<List<CommentVO>> getCommentsByArticleId(@PathVariable String articleId) {
        log.info("获取文章{}的评论列表", articleId);

        // 尝试获取当前用户ID（用户可能未登录）
        String userId = null;
        try {
            userId = BaseContext.getCurrentId();
        } catch (Exception e) {
            log.debug("用户未登录，无法判断点赞状态");
        }

        List<CommentVO> comments = commentService.getCommentsByArticleId(articleId, userId);
        return Result.success(comments);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/comment/{commentId}")
    @ApiOperation("删除评论")
    public Result<String> deleteComment(@PathVariable String commentId) {
        String userId = BaseContext.getCurrentId();
        log.info("用户{}删除评论{}", userId, commentId);

        commentService.deleteComment(commentId, userId);
        return Result.success();
    }

    /**
     * 点赞/取消点赞评论
     */
    @PostMapping("/comment/{commentId}/like")
    @ApiOperation("点赞/取消点赞评论")
    public Result<Map<String, Object>> toggleCommentLike(@PathVariable String commentId) {
        String userId = BaseContext.getCurrentId();
        log.info("用户{}操作评论{}的点赞", userId, commentId);

        Map<String, Object> result = commentLikeService.toggleLike(userId, commentId);
        return Result.success(result);
    }

    /**
     * 查询用户是否已点赞评论
     */
    @GetMapping("/comment/{commentId}/like/status")
    @ApiOperation("查询是否已点赞评论")
    public Result<Boolean> checkCommentLikeStatus(@PathVariable String commentId) {
        String userId = BaseContext.getCurrentId();
        log.info("查询用户{}是否点赞评论{}", userId, commentId);

        Boolean isLiked = commentLikeService.isLiked(userId, commentId);
        return Result.success(isLiked);
    }
}

