package com.eaxon.coderhubserver.controller.admin;

import com.eaxon.coderhubcommon.result.Result;
import com.eaxon.coderhubpojo.VO.ArticleVO;
import com.eaxon.coderhubpojo.entity.Article;
import com.eaxon.coderhubpojo.entity.Tag;
import com.eaxon.coderhubserver.mapper.ArticleMapper;
import com.eaxon.coderhubserver.mapper.CategoryMapper;
import com.eaxon.coderhubserver.mapper.TagMapper;
import com.eaxon.coderhubserver.mapper.UserMapper;
import com.eaxon.coderhubpojo.entity.Category;
import com.eaxon.coderhubpojo.entity.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/admin/article")
@Slf4j
@Api(tags = "管理端-文章管理接口")
public class ArticleManagerController {
    
    @Autowired
    private ArticleMapper articleMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    @Autowired
    private TagMapper tagMapper;
    
    /**
     * 获取待审核文章列表
     */
    @GetMapping("/pending")
    @ApiOperation("获取待审核文章列表")
    public Result<List<ArticleVO>> getPendingArticles() {
        log.info("获取待审核文章列表");
        List<Article> articles = articleMapper.getPendingArticles();
        List<ArticleVO> voList = buildArticleVOList(articles);
        return Result.success(voList);
    }
    
    /**
     * 获取已发布文章列表
     */
    @GetMapping("/published")
    @ApiOperation("获取已发布文章列表")
    public Result<List<ArticleVO>> getPublishedArticles() {
        log.info("获取已发布文章列表");
        List<Article> articles = articleMapper.getPublishedArticles();
        List<ArticleVO> voList = buildArticleVOList(articles);
        return Result.success(voList);
    }
    
    /**
     * 获取文章详情（含Markdown内容URL）
     */
    @GetMapping("/{id}")
    @ApiOperation("获取文章详情")
    public Result<ArticleVO> getArticleDetail(@PathVariable String id) {
        log.info("获取文章详情：id={}", id);
        Article article = articleMapper.getById(id);
        if (article == null) {
            return Result.error("文章不存在");
        }
        
        // 构建VO
        User user = userMapper.getUserById(article.getUserId());
        Category category = categoryMapper.findById(article.getCategoryId());
        List<Tag> tags = tagMapper.getByArticleId(id);
        
        ArticleVO vo = ArticleVO.builder()
                .id(article.getId())
                .userId(article.getUserId())
                .username(user != null ? user.getUsername() : "未知用户")
                .avatar(user != null ? user.getAvatar() : null)
                .title(article.getTitle())
                .summary(article.getSummary())
                .contentUrl(article.getContentUrl())  // 返回Markdown URL
                .coverImage(article.getCoverImage())
                .categoryId(article.getCategoryId())
                .categoryName(category != null ? category.getCategoryName() : null)
                .tags(tags)
                .viewCount(article.getViewCount())
                .likeCount(article.getLikeCount())
                .commentCount(article.getCommentCount())
                .collectCount(article.getCollectCount())
                .isTop(article.getIsTop())
                .isOriginal(article.getIsOriginal())
                .status(article.getStatus())
                .auditStatus(article.getAuditStatus())
                .auditReason(article.getAuditReason())
                .createTime(article.getCreateTime())
                .publishTime(article.getPublishTime())
                .build();
        
        return Result.success(vo);
    }
    
    /**
     * 审核文章（通过）
     */
    @PutMapping("/approve/{id}")
    @ApiOperation("审核通过")
    public Result<String> approveArticle(@PathVariable String id) {
        log.info("审核通过文章：id={}", id);
        
        // 获取文章信息
        Article article = articleMapper.getById(id);
        if (article == null) {
            return Result.error("文章不存在");
        }
        
        // 更新审核状态
        articleMapper.updateAuditStatus(id, 1, null, 1);
        
        // 更新分类文章数量（包括父分类）
        if (article.getCategoryId() != null) {
            // 更新当前分类
            categoryMapper.incrementArticleCount(article.getCategoryId());
            
            // 获取当前分类信息，更新父分类
            Category category = categoryMapper.findById(article.getCategoryId());
            if (category != null && category.getParentId() != null) {
                categoryMapper.incrementArticleCount(category.getParentId());
            }
        }
        
        return Result.success("审核通过");
    }
    
    /**
     * 审核文章（拒绝）
     */
    @PutMapping("/reject/{id}")
    @ApiOperation("审核拒绝")
    public Result<String> rejectArticle(@PathVariable String id, @RequestParam String reason) {
        log.info("审核拒绝文章：id={}, reason={}", id, reason);
        articleMapper.updateAuditStatus(id, 2, reason, 3);  // status=3表示审核未通过
        return Result.success("审核已拒绝");
    }
    
    /**
     * 修复所有分类的文章数量统计
     */
    @PutMapping("/fix-category-count")
    @ApiOperation("修复分类文章数量")
    public Result<String> fixCategoryCount() {
        log.info("开始修复分类文章数量统计");
        
        try {
            // 获取所有分类
            List<Category> categories = categoryMapper.findAll();
            
            for (Category category : categories) {
                // 统计该分类下已发布且审核通过的文章数量
                int count = articleMapper.countByCategoryIdAndStatus(category.getId(), 1, 1);
                
                // 更新分类的文章数量
                category.setArticleCount(count);
                categoryMapper.update(category);
                
                log.info("分类[{}]文章数量更新为：{}", category.getCategoryName(), count);
            }
            
            return Result.success("分类文章数量修复完成");
        } catch (Exception e) {
            log.error("修复分类文章数量失败", e);
            return Result.error("修复失败：" + e.getMessage());
        }
    }
    
    /**
     * 构建ArticleVO列表
     */
    private List<ArticleVO> buildArticleVOList(List<Article> articles) {
        List<ArticleVO> voList = new ArrayList<>();
        for (Article article : articles) {
            User user = userMapper.getUserById(article.getUserId());
            Category category = categoryMapper.findById(article.getCategoryId());
            List<Tag> tags = tagMapper.getByArticleId(article.getId());
            
            ArticleVO vo = ArticleVO.builder()
                    .id(article.getId())
                    .userId(article.getUserId())
                    .username(user != null ? user.getUsername() : "未知用户")
                    .avatar(user != null ? user.getAvatar() : null)
                    .title(article.getTitle())
                    .summary(article.getSummary())
                    .contentUrl(article.getContentUrl())  // 添加contentUrl
                    .coverImage(article.getCoverImage())
                    .categoryId(article.getCategoryId())
                    .categoryName(category != null ? category.getCategoryName() : null)
                    .tags(tags)
                    .viewCount(article.getViewCount())
                    .likeCount(article.getLikeCount())
                    .commentCount(article.getCommentCount())
                    .collectCount(article.getCollectCount())
                    .isTop(article.getIsTop())
                    .isOriginal(article.getIsOriginal())
                    .status(article.getStatus())
                    .auditStatus(article.getAuditStatus())
                    .auditReason(article.getAuditReason())
                    .createTime(article.getCreateTime())
                    .publishTime(article.getPublishTime())
                    .build();
            
            voList.add(vo);
        }
        return voList;
    }
}