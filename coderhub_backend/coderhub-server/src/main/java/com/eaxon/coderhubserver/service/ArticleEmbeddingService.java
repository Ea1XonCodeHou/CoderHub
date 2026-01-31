package com.eaxon.coderhubserver.service;

import com.eaxon.coderhubpojo.entity.Article;
import com.eaxon.coderhubpojo.entity.Category;
import com.eaxon.coderhubpojo.entity.Tag;
import com.eaxon.coderhubpojo.entity.User;
import com.eaxon.coderhubserver.mapper.ArticleMapper;
import com.eaxon.coderhubserver.mapper.CategoryMapper;
import com.eaxon.coderhubserver.mapper.TagMapper;
import com.eaxon.coderhubserver.mapper.UserMapper;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 文章向量化服务
 * 
 * 负责将文章内容向量化并存入ChromaDB，用于RAG语义检索
 */
@Service
@Slf4j
public class ArticleEmbeddingService {

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /** Redis中记录已向量化文章的Set */
    private static final String EMBEDDED_ARTICLES_KEY = "embedding:articles";

    /**
     * 向量化并存储单篇文章（同步）
     * 
     * 避免重复策略：使用Redis Set记录已向量化的articleId
     * 
     * @param articleId 文章ID
     * @return 是否成功
     */
    public boolean embedArticle(String articleId) {
        return embedArticle(articleId, false);
    }

    /**
     * 向量化并存储单篇文章（支持强制更新）
     * 
     * @param articleId 文章ID
     * @param forceUpdate 是否强制更新（忽略去重检查）
     * @return 是否成功
     */
    public boolean embedArticle(String articleId, boolean forceUpdate) {
        try {
            // 去重检查：如果已向量化且非强制更新，则跳过
            if (!forceUpdate && isArticleEmbedded(articleId)) {
                log.info("文章已向量化，跳过: articleId={}", articleId);
                return true;
            }

            Article article = articleMapper.getById(articleId);
            if (article == null) {
                log.warn("向量化失败：文章不存在, articleId={}", articleId);
                return false;
            }

            // 只向量化已发布的文章
            if (article.getStatus() != 1) {
                log.info("跳过未发布文章: articleId={}, status={}", articleId, article.getStatus());
                return false;
            }

            boolean success = doEmbedArticle(article);
            
            // 成功后记录到Redis
            if (success) {
                markArticleAsEmbedded(articleId);
            }
            
            return success;
        } catch (Exception e) {
            log.error("向量化文章失败: articleId={}, error={}", articleId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 向量化并存储单篇文章（异步）
     * 
     * 适用于文章发布/更新后异步执行，不阻塞主流程
     * 
     * @param articleId 文章ID
     */
    @Async
    public void embedArticleAsync(String articleId) {
        log.info("异步向量化文章: articleId={}", articleId);
        embedArticle(articleId);
    }

    /**
     * 批量向量化所有已发布文章
     * 
     * @return 成功向量化的数量
     */
    public int batchEmbedAllArticles() {
        log.info("开始批量向量化所有已发布文章...");
        
        List<Article> articles = articleMapper.getPublishedArticles();
        if (articles == null || articles.isEmpty()) {
            log.info("没有需要向量化的文章");
            return 0;
        }

        int successCount = 0;
        int totalCount = articles.size();
        
        for (int i = 0; i < totalCount; i++) {
            Article article = articles.get(i);
            try {
                boolean success = doEmbedArticle(article);
                if (success) {
                    successCount++;
                }
                
                // 每处理10篇输出进度
                if ((i + 1) % 10 == 0 || i == totalCount - 1) {
                    log.info("向量化进度: {}/{}, 成功: {}", i + 1, totalCount, successCount);
                }
            } catch (Exception e) {
                log.error("向量化文章失败: articleId={}, error={}", article.getId(), e.getMessage());
            }
        }

        log.info("批量向量化完成: 总计={}, 成功={}, 失败={}", totalCount, successCount, totalCount - successCount);
        return successCount;
    }

    /**
     * 删除文章向量（文章删除时调用）
     * 
     * TODO: LangChain4j标准接口不支持按metadata条件删除
     * 目前采用标记方式，后续可通过ChromaDB原生API实现
     * 
     * @param articleId 文章ID
     */
    public void removeArticleEmbedding(String articleId) {
        // 由于LangChain4j接口限制，暂时只记录日志
        // 后续可通过定时任务清理孤儿向量
        log.info("标记删除文章向量（需后续清理）: articleId={}", articleId);
    }

    /**
     * 检查文章是否已向量化
     * 
     * 通过Redis Set判断
     * 
     * @param articleId 文章ID
     * @return 是否已向量化
     */
    public boolean isArticleEmbedded(String articleId) {
        try {
            Boolean isMember = redisTemplate.opsForSet().isMember(EMBEDDED_ARTICLES_KEY, articleId);
            return Boolean.TRUE.equals(isMember);
        } catch (Exception e) {
            log.warn("检查向量化状态失败: articleId={}, error={}", articleId, e.getMessage());
            return false;
        }
    }

    /**
     * 标记文章已向量化（记录到Redis Set）
     */
    private void markArticleAsEmbedded(String articleId) {
        try {
            redisTemplate.opsForSet().add(EMBEDDED_ARTICLES_KEY, articleId);
        } catch (Exception e) {
            log.warn("记录向量化状态失败: articleId={}, error={}", articleId, e.getMessage());
        }
    }

    /**
     * 移除文章的向量化标记（用于重新向量化）
     */
    public void unmarkArticleAsEmbedded(String articleId) {
        try {
            redisTemplate.opsForSet().remove(EMBEDDED_ARTICLES_KEY, articleId);
        } catch (Exception e) {
            log.warn("移除向量化标记失败: articleId={}, error={}", articleId, e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 执行向量化操作
     * 
     * 注意：由于LangChain4j接口限制，无法按articleId删除旧向量
     * 重复向量化会产生多条记录，但检索时通过metadata.articleId去重
     */
    private boolean doEmbedArticle(Article article) {
        String articleId = article.getId();

        // 1. 构建文章文本（标题 + 摘要 + 标签）
        String articleText = buildArticleText(article);
        if (articleText == null || articleText.trim().isEmpty()) {
            log.warn("文章内容为空，跳过: articleId={}", articleId);
            return false;
        }

        // 2. 构建元数据（包含articleId用于后续去重和关联）
        Metadata metadata = buildMetadata(article);

        // 3. 创建TextSegment
        TextSegment segment = TextSegment.from(articleText, metadata);

        // 4. 生成向量
        Embedding embedding = embeddingModel.embed(segment).content();

        // 5. 存储到ChromaDB
        embeddingStore.add(embedding, segment);

        log.info("向量化成功: articleId={}, title={}", articleId, article.getTitle());
        return true;
    }

    /**
     * 构建用于向量化的文章文本
     * 
     * 格式：标题 + 摘要 + 标签
     */
    private String buildArticleText(Article article) {
        StringBuilder sb = new StringBuilder();
        
        // 标题
        if (article.getTitle() != null) {
            sb.append(article.getTitle()).append("\n\n");
        }
        
        // 摘要
        if (article.getSummary() != null) {
            sb.append(article.getSummary()).append("\n\n");
        }
        
        // 标签
        List<Tag> tags = tagMapper.getByArticleId(article.getId());
        if (tags != null && !tags.isEmpty()) {
            String tagStr = tags.stream()
                    .map(Tag::getTagName)
                    .collect(Collectors.joining(", "));
            sb.append("标签: ").append(tagStr);
        }
        
        return sb.toString().trim();
    }

    /**
     * 构建向量元数据（用于检索时过滤和展示）
     */
    private Metadata buildMetadata(Article article) {
        Metadata metadata = new Metadata();
        
        metadata.put("articleId", article.getId());
        metadata.put("title", article.getTitle() != null ? article.getTitle() : "");
        metadata.put("userId", article.getUserId() != null ? article.getUserId() : "");
        
        // 作者名称
        if (article.getUserId() != null) {
            User user = userMapper.getUserById(article.getUserId());
            if (user != null) {
                metadata.put("authorName", user.getUsername() != null ? user.getUsername() : "");
            }
        }
        
        // 分类名称
        if (article.getCategoryId() != null) {
            Category category = categoryMapper.findById(article.getCategoryId());
            if (category != null) {
                metadata.put("categoryId", article.getCategoryId());
                metadata.put("categoryName", category.getCategoryName() != null ? category.getCategoryName() : "");
            }
        }
        
        // 发布时间
        if (article.getPublishTime() != null) {
            metadata.put("publishTime", article.getPublishTime().toString());
        }
        
        // 统计数据
        metadata.put("viewCount", String.valueOf(article.getViewCount() != null ? article.getViewCount() : 0));
        metadata.put("likeCount", String.valueOf(article.getLikeCount() != null ? article.getLikeCount() : 0));
        
        return metadata;
    }
}

