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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 文章向量化服务（Chunking + Overlap）
 *
 * 将文章正文从 OSS 下载，分段后向量化存入 ChromaDB。
 * 每篇文章生成 N 个 chunk 向量，检索时命中的是段落而非整篇文章。
 */
@Service
@Slf4j
public class ArticleEmbeddingService {

    @Autowired
    private volatile EmbeddingStore<TextSegment> embeddingStore;

    /**
     * 刷新 EmbeddingStore 引用（清空 ChromaDB 重建 collection 后调用）
     */
    public void refreshEmbeddingStore(EmbeddingStore<TextSegment> newStore) {
        this.embeddingStore = newStore;
        log.info("EmbeddingStore 引用已刷新");
    }

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
    private OssContentService ossContentService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String EMBEDDED_ARTICLES_KEY = "embedding:articles";

    /** 目标 chunk 大小（中文字符数） */
    private static final int CHUNK_SIZE = 512;
    /** overlap 字符数（约 10%） */
    private static final int OVERLAP_SIZE = 50;

    // ==================== 公开接口 ====================

    public boolean embedArticle(String articleId) {
        return embedArticle(articleId, false);
    }

    public boolean embedArticle(String articleId, boolean forceUpdate) {
        try {
            if (!forceUpdate && isArticleEmbedded(articleId)) {
                log.info("文章已向量化，跳过: articleId={}", articleId);
                return true;
            }

            Article article = articleMapper.getById(articleId);
            if (article == null) {
                log.warn("向量化失败：文章不存在, articleId={}", articleId);
                return false;
            }
            if (article.getStatus() != 1) {
                log.info("跳过未发布文章: articleId={}, status={}", articleId, article.getStatus());
                return false;
            }

            boolean success = doEmbedArticle(article);
            if (success) {
                markArticleAsEmbedded(articleId);
            }
            return success;
        } catch (Exception e) {
            log.error("向量化文章失败: articleId={}, error={}", articleId, e.getMessage(), e);
            return false;
        }
    }

    @Async
    public void embedArticleAsync(String articleId) {
        log.info("异步向量化文章: articleId={}", articleId);
        embedArticle(articleId);
    }

    public int batchEmbedAllArticles() {
        log.info("开始批量向量化所有已发布文章（Chunking模式）...");

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
                    markArticleAsEmbedded(article.getId());
                }
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
     * 清空 Redis 中的向量化记录（配合重建索引使用）
     */
    public void clearEmbeddingRecords() {
        try {
            redisTemplate.delete(EMBEDDED_ARTICLES_KEY);
            log.info("已清空向量化记录 Redis Set");
        } catch (Exception e) {
            log.warn("清空向量化记录失败: {}", e.getMessage());
        }
    }

    public void removeArticleEmbedding(String articleId) {
        log.info("标记删除文章向量（需后续清理）: articleId={}", articleId);
        unmarkArticleAsEmbedded(articleId);
    }

    public boolean isArticleEmbedded(String articleId) {
        try {
            Boolean isMember = redisTemplate.opsForSet().isMember(EMBEDDED_ARTICLES_KEY, articleId);
            return Boolean.TRUE.equals(isMember);
        } catch (Exception e) {
            log.warn("检查向量化状态失败: articleId={}", articleId);
            return false;
        }
    }

    // ==================== 核心逻辑 ====================

    private boolean doEmbedArticle(Article article) {
        String articleId = article.getId();

        // 1. 从 OSS 下载正文
        String markdownContent = downloadArticleContent(article);
        if (markdownContent == null || markdownContent.trim().isEmpty()) {
            log.warn("文章正文为空，回退到标题+摘要模式: articleId={}", articleId);
            markdownContent = buildFallbackText(article);
        }

        // 2. 清洗 Markdown
        String cleanText = cleanMarkdown(markdownContent);
        if (cleanText.trim().length() < 20) {
            log.warn("文章内容过短，跳过: articleId={}", articleId);
            return false;
        }

        // 3. 分段（Chunking + 10% Overlap）
        List<String> chunks = chunkText(cleanText, CHUNK_SIZE, OVERLAP_SIZE);
        log.info("文章分段完成: articleId={}, chunks={}", articleId, chunks.size());

        // 4. 构建元数据
        Metadata baseMetadata = buildMetadata(article);

        // 5. 向量化每个 chunk 并存入 ChromaDB
        int storedCount = 0;
        for (int i = 0; i < chunks.size(); i++) {
            String chunkText = chunks.get(i);
            Metadata chunkMeta = Metadata.from(baseMetadata.toMap());
            chunkMeta.put("chunkIndex", String.valueOf(i));
            chunkMeta.put("totalChunks", String.valueOf(chunks.size()));

            TextSegment segment = TextSegment.from(chunkText, chunkMeta);
            try {
                Embedding embedding = embeddingModel.embed(segment).content();
                embeddingStore.add(embedding, segment);
                storedCount++;
            } catch (Exception e) {
                log.warn("chunk 向量化失败: articleId={}, chunk={}, error={}", articleId, i, e.getMessage());
            }
        }

        log.info("向量化成功: articleId={}, title={}, chunks={}/{}", articleId, article.getTitle(), storedCount, chunks.size());
        return storedCount > 0;
    }

    /**
     * 从 OSS 下载文章正文
     */
    private String downloadArticleContent(Article article) {
        String contentUrl = article.getContentUrl();
        if (contentUrl == null || contentUrl.isEmpty()) {
            return null;
        }
        try {
            return ossContentService.downloadMarkdownContent(contentUrl);
        } catch (Exception e) {
            log.warn("下载文章正文失败: articleId={}, url={}", article.getId(), contentUrl);
            return null;
        }
    }

    /**
     * 回退：无法获取正文时用标题+摘要+标签
     */
    private String buildFallbackText(Article article) {
        StringBuilder sb = new StringBuilder();
        if (article.getTitle() != null) sb.append(article.getTitle()).append("\n\n");
        if (article.getSummary() != null) sb.append(article.getSummary()).append("\n\n");
        List<Tag> tags = tagMapper.getByArticleId(article.getId());
        if (tags != null && !tags.isEmpty()) {
            sb.append("标签: ").append(tags.stream().map(Tag::getTagName).collect(Collectors.joining(", ")));
        }
        return sb.toString().trim();
    }

    /**
     * 清洗 Markdown 文本，保留语义内容
     * 去掉图片、链接URL、代码块标记符，但保留代码内容和标题文本
     */
    private String cleanMarkdown(String markdown) {
        if (markdown == null) return "";
        String text = markdown;
        // 去掉图片 ![alt](url)
        text = text.replaceAll("!\\[.*?]\\(.*?\\)", "");
        // 链接保留文本 [text](url) → text
        text = text.replaceAll("\\[([^]]*)]\\([^)]*\\)", "$1");
        // 去掉代码块标记 ```lang ... ``` → 保留内容
        text = text.replaceAll("```\\w*\\n?", "");
        // 去掉行内代码标记
        text = text.replaceAll("`([^`]*)`", "$1");
        // 去掉 HTML 标签
        text = text.replaceAll("<[^>]+>", "");
        // Markdown 标题符号 # → 保留文字
        text = text.replaceAll("(?m)^#{1,6}\\s+", "");
        // 去掉加粗/斜体标记
        text = text.replaceAll("\\*{1,3}([^*]+)\\*{1,3}", "$1");
        // 压缩连续空行
        text = text.replaceAll("\n{3,}", "\n\n");
        return text.trim();
    }

    /**
     * 将文本分成固定大小的 Chunk，相邻 Chunk 有 Overlap
     * 优先按段落边界切割，超长段落按句子切割
     */
    static List<String> chunkText(String text, int chunkSize, int overlapSize) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) return chunks;

        // 按段落分割（双换行或单换行都算段落边界）
        String[] paragraphs = text.split("\n{1,}");
        StringBuilder currentChunk = new StringBuilder();

        for (String para : paragraphs) {
            String trimmed = para.trim();
            if (trimmed.isEmpty()) continue;

            // 如果当前段落加上去不超限，直接追加
            if (currentChunk.length() + trimmed.length() + 1 <= chunkSize) {
                if (currentChunk.length() > 0) currentChunk.append("\n");
                currentChunk.append(trimmed);
            } else {
                // 当前 chunk 已满，保存并开始新 chunk
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString());
                    // overlap：取当前 chunk 末尾部分作为下一个 chunk 的开头
                    String overlapText = getOverlapTail(currentChunk.toString(), overlapSize);
                    currentChunk = new StringBuilder(overlapText);
                }

                // 如果单个段落就超过 chunkSize，按句子再切
                if (trimmed.length() > chunkSize) {
                    List<String> subChunks = splitLongParagraph(trimmed, chunkSize, overlapSize);
                    for (int i = 0; i < subChunks.size(); i++) {
                        if (i == subChunks.size() - 1) {
                            // 最后一段留给 currentChunk 继续合并后续段落
                            currentChunk = new StringBuilder(subChunks.get(i));
                        } else {
                            chunks.add(subChunks.get(i));
                        }
                    }
                } else {
                    if (currentChunk.length() > 0) currentChunk.append("\n");
                    currentChunk.append(trimmed);
                }
            }
        }

        // 最后一个 chunk
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString());
        }

        return chunks;
    }

    /**
     * 超长段落按句子边界切割
     */
    private static List<String> splitLongParagraph(String text, int chunkSize, int overlapSize) {
        List<String> result = new ArrayList<>();
        // 中英文句子边界
        Pattern sentenceEnd = Pattern.compile("[。！？；.!?;]");
        Matcher matcher = sentenceEnd.matcher(text);

        StringBuilder chunk = new StringBuilder();

        int lastEnd = 0;
        while (matcher.find()) {
            String sentence = text.substring(lastEnd, matcher.end());
            lastEnd = matcher.end();

            if (chunk.length() + sentence.length() <= chunkSize) {
                chunk.append(sentence);
            } else {
                if (chunk.length() > 0) {
                    result.add(chunk.toString());
                    String overlap = getOverlapTail(chunk.toString(), overlapSize);
                    chunk = new StringBuilder(overlap);
                }
                chunk.append(sentence);
            }
        }
        // 剩余文本
        if (lastEnd < text.length()) {
            chunk.append(text.substring(lastEnd));
        }
        if (chunk.length() > 0) {
            result.add(chunk.toString());
        }

        return result;
    }

    /**
     * 获取文本末尾 N 个字符作为 overlap
     */
    private static String getOverlapTail(String text, int overlapSize) {
        if (text.length() <= overlapSize) return text;
        return text.substring(text.length() - overlapSize);
    }

    // ==================== 元数据 ====================

    private Metadata buildMetadata(Article article) {
        Metadata metadata = new Metadata();
        metadata.put("articleId", article.getId());
        metadata.put("title", article.getTitle() != null ? article.getTitle() : "");
        metadata.put("userId", article.getUserId() != null ? article.getUserId() : "");

        if (article.getUserId() != null) {
            User user = userMapper.getUserById(article.getUserId());
            if (user != null && user.getUsername() != null) {
                metadata.put("authorName", user.getUsername());
            }
        }

        if (article.getCategoryId() != null) {
            Category category = categoryMapper.findById(article.getCategoryId());
            if (category != null) {
                metadata.put("categoryId", article.getCategoryId());
                metadata.put("categoryName", category.getCategoryName() != null ? category.getCategoryName() : "");
            }
        }

        if (article.getPublishTime() != null) {
            metadata.put("publishTime", article.getPublishTime().toString());
        }

        metadata.put("viewCount", String.valueOf(article.getViewCount() != null ? article.getViewCount() : 0));
        metadata.put("likeCount", String.valueOf(article.getLikeCount() != null ? article.getLikeCount() : 0));

        return metadata;
    }

    // ==================== Redis 标记 ====================

    private void markArticleAsEmbedded(String articleId) {
        try {
            redisTemplate.opsForSet().add(EMBEDDED_ARTICLES_KEY, articleId);
        } catch (Exception e) {
            log.warn("记录向量化状态失败: articleId={}", articleId);
        }
    }

    public void unmarkArticleAsEmbedded(String articleId) {
        try {
            redisTemplate.opsForSet().remove(EMBEDDED_ARTICLES_KEY, articleId);
        } catch (Exception e) {
            log.warn("移除向量化标记失败: articleId={}", articleId);
        }
    }
}
