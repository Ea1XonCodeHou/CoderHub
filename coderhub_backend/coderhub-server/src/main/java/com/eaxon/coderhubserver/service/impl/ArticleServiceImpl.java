package com.eaxon.coderhubserver.service.impl;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eaxon.coderhubcommon.context.BaseContext;
import com.eaxon.coderhubcommon.exception.BaseException;
import com.eaxon.coderhubcommon.utils.AliOssUtil;
import com.eaxon.coderhubpojo.DTO.ArticlePublishDTO;
import com.eaxon.coderhubpojo.VO.ArticleDetailVO;
import com.eaxon.coderhubpojo.VO.ArticleVO;
import com.eaxon.coderhubpojo.entity.Article;
import com.eaxon.coderhubpojo.entity.ArticleTagRelation;
import com.eaxon.coderhubpojo.entity.Category;
import com.eaxon.coderhubpojo.entity.Tag;
import com.eaxon.coderhubpojo.entity.User;
import com.eaxon.coderhubserver.mapper.ArticleMapper;
import com.eaxon.coderhubserver.mapper.ArticleTagRelationMapper;
import com.eaxon.coderhubserver.mapper.CategoryMapper;
import com.eaxon.coderhubserver.mapper.TagMapper;
import com.eaxon.coderhubserver.mapper.UserMapper;
import com.eaxon.coderhubserver.service.ArticleEmbeddingService;
import com.eaxon.coderhubserver.service.ArticleService;
import com.eaxon.coderhubserver.service.RedisService;

import lombok.extern.slf4j.Slf4j;

/**
 * 文章Service实现类
 */
@Service
@Slf4j
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private ArticleTagRelationMapper relationMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private AliOssUtil aliOssUtil;
    
    @Autowired
    private RedisService redisService;
    
    @Autowired
    private ArticleEmbeddingService articleEmbeddingService;

    /**
     * 发布文章
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleVO publishArticle(ArticlePublishDTO dto) {
        log.info("发布文章：{}", dto.getTitle());

        // 1. 上传Markdown内容到OSS
        String contentUrl = uploadMarkdownToOSS(dto.getContent());
        log.info("Markdown文件上传成功：{}", contentUrl);

        // 2. 生成摘要（如果没有传）
        String summary = dto.getSummary();
        if (summary == null || summary.isEmpty()) {
            summary = generateSummary(dto.getContent());
        }

        // 3. 构建Article对象
        Article article = Article.builder()
                .id(UUID.randomUUID().toString())
                .userId(BaseContext.getCurrentId())
                .title(dto.getTitle())
                .summary(summary)
                .contentUrl(contentUrl)
                .coverImage(dto.getCoverImage())
                .categoryId(dto.getCategoryId())
                .status(2)  // 默认待审核状态
                .isOriginal(dto.getIsOriginal() != null ? dto.getIsOriginal() : 1)
                .isTop(0)
                .viewCount(0L)
                .likeCount(0)
                .commentCount(0)
                .collectCount(0)
                .auditStatus(0)  // 默认待审核
                .publishTime(null)  // 审核通过后才设置发布时间
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        // 4. 保存文章
        articleMapper.insert(article);
        
        // 4.5 初始化Redis浏览量为0
        redisService.setViewCount(article.getId(), 0L);
        log.debug("初始化Redis浏览量：articleId={}", article.getId());

        // 5. 处理标签
        List<Tag> tags = new ArrayList<>();
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            tags = processArticleTags(article.getId(), dto.getTags());
        }

        // 6. 构建返回VO
        ArticleVO vo = buildArticleVO(article, tags);

        log.info("文章发布成功：articleId={}", article.getId());
        return vo;
    }

    /**
     * 获取文章详情
     */
    @Override
    public ArticleDetailVO getArticleDetail(String articleId) {
        log.info("获取文章详情：articleId={}", articleId);

        // 1. 查询文章
        Article article = articleMapper.getById(articleId);
        if (article == null) {
            throw new BaseException("文章不存在");
        }

        // 2. 查询作者信息
        User user = userMapper.getUserById(article.getUserId());

        // 3. 查询分类信息
        Category category = null;
        if (article.getCategoryId() != null) {
            category = categoryMapper.findById(article.getCategoryId());
        }

        // 4. 查询标签
        List<Tag> tags = tagMapper.getByArticleId(articleId);

        // 5. Redis浏览量+1（原子操作）
        Long newViewCount = redisService.incrementViewCount(articleId);
        
        // 如果Redis返回null（Redis故障或key不存在），降级到MySQL
        if (newViewCount == null) {
            log.warn("Redis获取浏览量失败，降级到MySQL：articleId={}", articleId);
            // 降级：直接更新MySQL
            articleMapper.incrementViewCount(articleId);
            // 从MySQL重新加载并写入Redis
            Article reloadedArticle = articleMapper.getById(articleId);
            newViewCount = reloadedArticle.getViewCount();
            redisService.setViewCount(articleId, newViewCount);
        } else if (newViewCount == 1) {
            // Redis返回1，可能是新Key或数据丢失
            // 从MySQL加载真实浏览量
            Long dbViewCount = article.getViewCount();
            if (dbViewCount > 0) {
                // 数据库有值，说明Redis丢失了，重新设置
                log.info("Redis浏览量丢失，从MySQL恢复：articleId={}, dbCount={}", articleId, dbViewCount);
                newViewCount = dbViewCount + 1;
                redisService.setViewCount(articleId, newViewCount);
            }
        }

        // 6. 构建VO
        ArticleDetailVO vo = ArticleDetailVO.builder()
                .id(article.getId())
                .userId(article.getUserId())
                .username(user != null ? user.getUsername() : "未知用户")
                .avatar(user != null ? user.getAvatar() : null)
                .title(article.getTitle())
                .summary(article.getSummary())
                .contentUrl(article.getContentUrl())
                .coverImage(article.getCoverImage())
                .categoryId(article.getCategoryId())
                .categoryName(category != null ? category.getCategoryName() : null)
                .tags(tags)
                .viewCount(newViewCount)  // 使用Redis的最新浏览量
                .likeCount(article.getLikeCount())
                .commentCount(article.getCommentCount())
                .collectCount(article.getCollectCount())
                .isTop(article.getIsTop())
                .isOriginal(article.getIsOriginal())
                .status(article.getStatus())
                .createTime(article.getCreateTime())
                .updateTime(article.getUpdateTime())
                .publishTime(article.getPublishTime())
                .build();

        return vo;
    }

    /**
     * 获取文章列表
     */
    @Override
    public List<ArticleVO> getArticleList(String categoryId, String tagId, Integer status) {
        log.info("获取文章列表：categoryId={}, tagId={}, status={}", categoryId, tagId, status);

        List<Article> articles;

        if (tagId != null) {
            // 根据标签查询
            articles = articleMapper.getByTagId(tagId);
        } else {
            // 根据分类和状态查询
            Article query = new Article();
            query.setCategoryId(categoryId);
            query.setStatus(status != null ? status : 1);  // 默认只查已发布
            articles = articleMapper.list(query);
        }
        
        if (articles == null || articles.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量获取文章ID列表
        List<String> articleIds = articles.stream()
                .map(Article::getId)
                .collect(Collectors.toList());
        
        // 批量从Redis获取浏览量（MGET，一次性获取）
        Map<String, Long> viewCountMap = redisService.batchGetViewCount(articleIds);
        log.debug("批量获取浏览量：{} 篇文章，Redis返回 {} 条", articleIds.size(), viewCountMap.size());

        // 批量查询标签（优化N+1问题）
        return articles.stream().map(article -> {
            List<Tag> tags = tagMapper.getByArticleId(article.getId());
            
            // 使用Redis的浏览量，如果Redis没有则使用数据库的
            Long viewCount = viewCountMap.get(article.getId());
            if (viewCount == null) {
                viewCount = article.getViewCount();
                // Redis没有，异步设置到Redis
                redisService.setViewCount(article.getId(), viewCount);
            }
            article.setViewCount(viewCount);
            
            return buildArticleVO(article, tags);
        }).collect(Collectors.toList());
    }

    /**
     * 删除文章
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticle(String articleId) {
        log.info("删除文章：articleId={}", articleId);

        // 1. 查询文章
        Article article = articleMapper.getById(articleId);
        if (article == null) {
            throw new BaseException("文章不存在");
        }

        // 2. 查询文章的所有标签
        List<Tag> tags = tagMapper.getByArticleId(articleId);

        // 3. 删除文章
        articleMapper.deleteById(articleId);

        // 4. 删除关联关系
        relationMapper.deleteByArticleId(articleId);

        // 5. 标签使用计数-1
        for (Tag tag : tags) {
            tagMapper.decrementUsageCount(tag.getId());
        }

        // 6. 删除OSS文件（可选，建议保留）
        try {
            String fileName = extractFileNameFromUrl(article.getContentUrl());
            aliOssUtil.delete(fileName);
            log.info("OSS文件删除成功：{}", fileName);
        } catch (Exception e) {
            log.warn("OSS文件删除失败：{}", e.getMessage());
        }
        
        // 7. 删除ChromaDB中的向量
        try {
            articleEmbeddingService.removeArticleEmbedding(articleId);
        } catch (Exception e) {
            log.warn("删除文章向量失败：{}", e.getMessage());
        }

        log.info("文章删除成功");
    }

    // ==================== 私有方法 ====================

    /**
     * 上传Markdown内容到OSS
     */
    private String uploadMarkdownToOSS(String content) {
        try {
            // 生成文件名：article/2024/10/21/uuid.md
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String fileName = "article/" + date + "/" + UUID.randomUUID() + ".md";

            // 上传文件
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            return aliOssUtil.upload(bytes, fileName);
        } catch (Exception e) {
            log.error("上传Markdown到OSS失败", e);
            throw new BaseException("文件上传失败");
        }
    }

    /**
     * 生成文章摘要（截取前200字）
     */
    private String generateSummary(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }

        // 去除Markdown语法符号
        String text = content
                .replaceAll("#", "")  // 标题
                .replaceAll("\\*", "")  // 粗体/斜体
                .replaceAll("```[\\s\\S]*?```", "")  // 代码块
                .replaceAll("`.*?`", "")  // 行内代码
                .replaceAll("\\[.*?\\]\\(.*?\\)", "")  // 链接
                .replaceAll("!\\[.*?\\]\\(.*?\\)", "")  // 图片
                .trim();

        // 截取前200字
        if (text.length() > 200) {
            return text.substring(0, 200) + "...";
        }
        return text;
    }

    /**
     * 处理文章标签
     */
    private List<Tag> processArticleTags(String articleId, List<String> tagNames) {
        List<Tag> tags = new ArrayList<>();

        for (String tagName : tagNames) {
            // 查询或创建标签
            Tag tag = tagMapper.getByName(tagName);
            if (tag == null) {
                // 不存在则创建
                tag = Tag.builder()
                        .id(UUID.randomUUID().toString())
                        .tagName(tagName)
                        .usageCount(1)
                        .referenceCount(1)
                        .viewCount(0L)
                        .status(1)
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build();
                tagMapper.insert(tag);
                log.info("创建新标签：{}", tagName);
            } else {
                // 存在则增加使用次数
                tagMapper.incrementUsageCount(tag.getId());
            }

            // 创建关联
            ArticleTagRelation relation = ArticleTagRelation.builder()
                    .id(UUID.randomUUID().toString())
                    .articleId(articleId)
                    .tagId(tag.getId())
                    .createTime(LocalDateTime.now())
                    .build();
            relationMapper.insert(relation);

            tags.add(tag);
        }

        return tags;
    }

    /**
     * 构建ArticleVO
     */
    private ArticleVO buildArticleVO(Article article, List<Tag> tags) {
        // 查询作者信息
        User user = userMapper.getUserById(article.getUserId());

        // 查询分类信息
        Category category = null;
        if (article.getCategoryId() != null) {
            category = categoryMapper.findById(article.getCategoryId());
        }

        return ArticleVO.builder()
                .id(article.getId())
                .userId(article.getUserId())
                .username(user != null ? user.getUsername() : "未知用户")
                .avatar(user != null ? user.getAvatar() : null)
                .title(article.getTitle())
                .summary(article.getSummary())
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
                .createTime(article.getCreateTime())
                .publishTime(article.getPublishTime())
                .build();
    }

    /**
     * 从URL提取文件名
     */
    private String extractFileNameFromUrl(String url) {
        // https://bucket.oss-cn-beijing.aliyuncs.com/article/2024/10/21/uuid.md
        // 提取：article/2024/10/21/uuid.md
        int index = url.lastIndexOf(".com/");
        if (index != -1) {
            return url.substring(index + 5);
        }
        return url;
    }
}

