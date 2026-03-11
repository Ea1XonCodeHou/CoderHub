package com.eaxon.coderhubserver.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.eaxon.coderhubcommon.constant.RedisConstant;
import com.eaxon.coderhubpojo.DTO.ChatRequestDTO;
import com.eaxon.coderhubpojo.DTO.ChatStreamEvent;
import com.eaxon.coderhubpojo.DTO.ChatStreamEvent.RecommendItem;
import com.eaxon.coderhubpojo.DTO.ChatStreamEvent.ToolCall;
import com.eaxon.coderhubpojo.entity.AIMessage;
import com.eaxon.coderhubpojo.entity.Article;
import com.eaxon.coderhubserver.agent.CoderHubTools;
import com.eaxon.coderhubserver.mapper.AIMessageMapper;
import com.eaxon.coderhubserver.mapper.ArticleMapper;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

/**
 * AI 对话服务层
 * 使用 LangChain4j 实现流式对话，支持工具调用
 * 
 * 核心功能：
 * 1. 流式响应 - 使用 Reactor Flux 实现真正的响应式流
 * 2. 工具调用 - 支持搜索教程、文章等工具
 * 3. 多模型支持 - 可动态切换不同的AI模型
 * 4. 上下文管理 - 支持对话历史
 * 
 * @author CoderHub
 */
@Service
@Slf4j
public class AIService {

    /**
     * ThreadLocal用于在Tool执行时传递原始JSON结果
     * 避免LLM改写导致无法提取结构化数据
     */
    public static final ThreadLocal<String> TOOL_RESULT_JSON = new ThreadLocal<>();
    public static final ThreadLocal<String> TOOL_NAME = new ThreadLocal<>();

    @Value("${langchain4j.open-ai.streaming-chat-model.api-key}")
    private String apiKey;

    @Value("${langchain4j.open-ai.streaming-chat-model.base-url}")
    private String baseUrl;

    @Value("${langchain4j.open-ai.streaming-chat-model.model-name:qwen-plus}")
    private String defaultModelName;

    @Autowired
    private CoderHubTools coderHubTools;

    @Autowired
    private AIMessageMapper messageMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private volatile EmbeddingStore<TextSegment> embeddingStore;

    /**
     * 刷新 EmbeddingStore 引用（清空 ChromaDB 重建后调用）
     */
    public void refreshEmbeddingStore(EmbeddingStore<TextSegment> newStore) {
        this.embeddingStore = newStore;
        log.info("AIService EmbeddingStore 引用已刷新");
    }

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private OssContentService ossContentService;

    /** 上下文窗口最大消息数 */
    private static final int MAX_CONTEXT_MESSAGES = 20;

    /** RAG检索返回的最大chunk数 */
    private static final int RAG_TOP_K = 8;

    /** BM25 关键词召回的最大文章数 */
    private static final int BM25_TOP_K = 5;

    /**
     * 流式模型缓存
     */
    private final Map<String, OpenAiStreamingChatModel> streamingModelCache = new ConcurrentHashMap<>();

    /**
     * 同步模型缓存（用于工具调用判断）
     */
    private final Map<String, OpenAiChatModel> syncModelCache = new ConcurrentHashMap<>();

    /**
     * 工具调用Agent接口
     */
    private CoderHubAgent coderHubAgent;

    /**
     * 普通对话系统提示词（无工具调用）
     */
    private static final String SYSTEM_PROMPT_SIMPLE = """
            你是 CoderHub AI 助手，一个专业的编程技术顾问。

            你具有以下特点：
            1. 专业知识：精通各种编程语言、框架和最佳实践
            2. 代码能力：能够编写清晰、高效、可维护的代码
            3. 沟通技巧：用清晰简洁的语言解释复杂概念

            在回答时请注意：
            - 提供准确、实用的技术建议
            - 代码示例要完整且可运行
            - 适当使用 Markdown 格式组织回答
            - 对于代码块，请标注编程语言以便语法高亮
            - 严禁编造不存在的平台内容、课程或文章
            """;

    /**
     * Agent接口定义
     */
    public interface CoderHubAgent {
        String chat(String message);
    }

    @PostConstruct
    public void init() {
        log.info("初始化 AI 服务，默认模型: {}, baseUrl: {}", defaultModelName, baseUrl);
        
        // 创建同步模型用于工具调用
        OpenAiChatModel syncModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(defaultModelName)
                .temperature(0.7)
                .maxTokens(4096)
                .logRequests(false)
                .logResponses(false)
                .build();
        
        // 使用AiServices构建Agent
        coderHubAgent = AiServices.builder(CoderHubAgent.class)
                .chatLanguageModel(syncModel)
                .tools(coderHubTools,  // 注册主工具类
                       coderHubTools.articleSearchSkill,  // 注册文章搜索技能
                       coderHubTools.articleContentReaderSkill,  // 注册文章阅读技能
                       coderHubTools.smartAssistantSkill)  // 注册智能助手技能
                .build();
        
        // 预热流式模型
        getOrCreateStreamingModel(defaultModelName, 0.7, 4096);
        
        log.info("AI 服务初始化完成，已加载工具: CoderHubTools + ArticleSearchSkill + ArticleContentReaderSkill + SmartAssistantSkill");
    }

    @PreDestroy
    public void destroy() {
        log.info("清理 AI 服务资源");
        streamingModelCache.clear();
        syncModelCache.clear();
    }

    /**
     * 流式对话 - 普通模式（无 Agent、无工具调用，直接流式输出）
     */
    public Flux<ChatStreamEvent> streamChat(ChatRequestDTO request) {
        String sessionId = request.getSessionId() != null ?
                request.getSessionId() : java.util.UUID.randomUUID().toString();
        String model = request.getModel() != null ? request.getModel() : defaultModelName;
        double temperature = request.getTemperature() != null ? request.getTemperature() : 0.7;
        int maxTokens = request.getMaxTokens() != null ? request.getMaxTokens() : 4096;

        log.info("开始普通流式对话 - sessionId: {}, model: {}", sessionId, model);

        return Flux.create(sink -> {
            try {
                // 直接获取流式模型，不做 Agent 同步调用
                OpenAiStreamingChatModel streamingModel = getOrCreateStreamingModel(model, temperature, maxTokens);

                // 构建消息列表（无工具结果）
                List<ChatMessage> messages = buildMessages(request, null);

                AtomicInteger tokenCount = new AtomicInteger(0);
                StringBuilder fullResponse = new StringBuilder();

                streamingModel.generate(messages, new StreamingResponseHandler<AiMessage>() {

                    @Override
                    public void onNext(String token) {
                        if (token != null && !token.isEmpty()) {
                            fullResponse.append(token);
                            tokenCount.incrementAndGet();
                            sink.next(ChatStreamEvent.message(token, sessionId));
                        }
                    }

                    @Override
                    public void onComplete(Response<AiMessage> response) {
                        log.info("流式响应完成 - sessionId: {}, 总字符数: {}",
                                sessionId, fullResponse.length());

                        ChatStreamEvent.TokenUsage usage = ChatStreamEvent.TokenUsage.builder()
                                .outputTokens(tokenCount.get())
                                .build();

                        sink.next(ChatStreamEvent.done(sessionId, usage));
                        sink.complete();
                    }

                    @Override
                    public void onError(Throwable error) {
                        log.error("流式响应出错 - sessionId: {}, error: {}", sessionId, error.getMessage());
                        sink.next(ChatStreamEvent.error(parseErrorMessage(error), sessionId));
                        sink.complete();
                    }
                });

            } catch (Exception e) {
                log.error("创建流式对话失败: {}", e.getMessage(), e);
                sink.next(ChatStreamEvent.error("服务初始化失败: " + e.getMessage(), sessionId));
                sink.complete();
            }
        }, FluxSink.OverflowStrategy.BUFFER);
    }

    /**
     * 从消息中提取搜索关键词
     */
    private String extractKeyword(String message) {
        if (message == null) return "";
        
        // 移除常见的意图词，保留技术关键词
        String[] removePatterns = {
            "我想学习?", "想学", "想了解", "帮我找", "帮我搜索", 
            "有什么", "有没有", "推荐一些", "推荐", "教程", "课程",
            "如何学", "怎么学", "入门", "请问", "请", "吗", "呢", "？", "?"
        };
        
        String result = message;
        for (String pattern : removePatterns) {
            result = result.replace(pattern, " ");
        }
        
        // 清理并返回
        return result.trim().replaceAll("\\s+", " ");
    }

    /**
     * 获取或创建流式模型
     */
    private OpenAiStreamingChatModel getOrCreateStreamingModel(String modelName, double temperature, int maxTokens) {
        String cacheKey = modelName + "_" + temperature + "_" + maxTokens;
        
        return streamingModelCache.computeIfAbsent(cacheKey, k -> {
            log.info("创建新的流式模型实例: {}", cacheKey);
            
            return OpenAiStreamingChatModel.builder()
                    .apiKey(apiKey)
                    .baseUrl(baseUrl)
                    .modelName(modelName)
                    .temperature(temperature)
                    .maxTokens(maxTokens)
                    .logRequests(false)
                    .logResponses(false)
                    .build();
        });
    }

    /**
     * 构建消息列表
     */
    private List<ChatMessage> buildMessages(ChatRequestDTO request, String toolResult) {
        List<ChatMessage> messages = new ArrayList<>();
        
        // 1. 添加系统提示词
        String systemPrompt = request.getSystemPrompt() != null ?
                request.getSystemPrompt() : SYSTEM_PROMPT_SIMPLE;
        messages.add(SystemMessage.from(systemPrompt));
        
        // 2. 从数据库加载历史对话（如果有 conversationId）
        String conversationId = request.getConversationId();
        if (conversationId != null && !conversationId.isEmpty()) {
            List<AIMessage> dbMessages = messageMapper.getRecentMessages(conversationId, MAX_CONTEXT_MESSAGES);
            for (AIMessage dbMsg : dbMessages) {
                if ("user".equalsIgnoreCase(dbMsg.getRole())) {
                    messages.add(UserMessage.from(dbMsg.getContent()));
                } else if ("assistant".equalsIgnoreCase(dbMsg.getRole())) {
                    messages.add(AiMessage.from(dbMsg.getContent()));
                }
            }
            log.debug("从数据库加载了 {} 条历史消息", dbMessages.size());
        } 
        // 兼容旧的 history 参数
        else if (request.getHistory() != null && !request.getHistory().isEmpty()) {
            for (ChatRequestDTO.ChatMessage historyMsg : request.getHistory()) {
                if ("user".equalsIgnoreCase(historyMsg.getRole())) {
                    messages.add(UserMessage.from(historyMsg.getContent()));
                } else if ("assistant".equalsIgnoreCase(historyMsg.getRole())) {
                    messages.add(AiMessage.from(historyMsg.getContent()));
                }
            }
        }
        
        // 3. 如果有工具调用结果，添加为上下文
        if (toolResult != null && !toolResult.isEmpty()) {
            String contextMessage = "【平台资源检索结果】\n" + toolResult + 
                    "\n\n请基于以上检索结果，结合你的专业知识，为用户提供学习建议和推荐。";
            messages.add(SystemMessage.from(contextMessage));
        }
        
        // 4. 添加当前用户消息
        messages.add(UserMessage.from(request.getMessage()));
        
        log.debug("构建消息列表完成，共 {} 条消息", messages.size());
        return messages;
    }

    /**
     * 解析错误消息
     */
    private String parseErrorMessage(Throwable error) {
        String message = error.getMessage();
        if (message == null) {
            return "未知错误";
        }
        
        if (message.contains("rate limit")) {
            return "请求频率过高，请稍后重试";
        }
        if (message.contains("timeout")) {
            return "请求超时，请稍后重试";
        }
        if (message.contains("unauthorized") || message.contains("401")) {
            return "API 认证失败，请检查配置";
        }
        if (message.contains("quota")) {
            return "API 配额已用完";
        }
        
        if (message.length() > 200) {
            return message.substring(0, 200) + "...";
        }
        
        return message;
    }

    // ==================== RAG 增强对话（HyDE + 混合召回） ====================

    /**
     * RAG增强系统提示词（检索片段模式）
     */
    private static final String RAG_SYSTEM_PROMPT = """
            你是 CoderHub AI 助手，一个专业的编程技术顾问。

            以下是从 CoderHub 知识库中检索到的与用户问题相关的文章段落：

            【知识库参考资料】
            %s

            回答要求：
            1. **仅基于上述参考资料回答**，不得编造、虚构任何不在上述资料中的文章、课程、教程标题或内容
            2. 如果参考资料中确实包含相关内容，直接引用原文标题和段落
            3. 如果参考资料不足以回答问题，明确告知用户"知识库中暂无相关内容"，然后基于你的通用知识给出建议，但不得伪装为平台内容
            4. 使用 Markdown 格式，代码块标注语言
            5. **绝对禁止**：编造文章标题、虚构课程名称、伪造推荐列表
            """;

    /**
     * RAG增强系统提示词（全文阅读模式 — 用户指定了文章ID）
     */
    private static final String RAG_FULLTEXT_SYSTEM_PROMPT = """
            你是 CoderHub AI 助手，一个专业的编程技术顾问。

            用户请求你深入阅读并讲解一篇平台文章。以下是该文章的完整内容（已从平台数据库获取）：

            【文章全文】
            标题：《%s》

            %s

            回答要求：
            1. 你已经成功获取了这篇文章的完整内容，请直接基于上述文章全文进行详细讲解
            2. 提炼文章的核心知识点，用结构化的方式呈现
            3. 对关键概念和代码示例做深入解读
            4. 使用 Markdown 格式，代码块标注语言
            5. 如果文章涉及实战内容，可以补充最佳实践建议
            """;

    /**
     * HyDE 提示词：让 LLM 生成假设性答案文档，用于向量检索
     */
    private static final String HYDE_PROMPT = """
            请根据以下问题，生成一段约150字的假设性技术文档片段。
            不需要完全正确，只需要语义上贴近可能的答案。直接输出文档片段，不要有其他说明。

            问题：%s
            """;

    /**
     * RAG增强的流式对话 - HyDE + 多路召回 + 真实进度反馈
     */
    public Flux<ChatStreamEvent> streamChatWithRAG(ChatRequestDTO request) {
        String sessionId = request.getSessionId() != null ?
                request.getSessionId() : java.util.UUID.randomUUID().toString();
        String model = request.getModel() != null ? request.getModel() : defaultModelName;
        double temperature = request.getTemperature() != null ? request.getTemperature() : 0.7;
        int maxTokens = request.getMaxTokens() != null ? request.getMaxTokens() : 4096;

        log.info("开始RAG增强对话（HyDE+混合召回） - sessionId: {}, model: {}", sessionId, model);
        String userMessage = request.getMessage();

        return Flux.create(sink -> {
            try {
                // 1. 发送思考中事件
                sink.next(ChatStreamEvent.thinking(sessionId, model));

                // 2. 检测用户消息中是否包含文章ID（UUID格式）
                String detectedArticleId = extractArticleId(userMessage);
                List<RetrievedArticle> retrievedChunks;

                if (detectedArticleId != null) {
                    // === 指定文章模式：直接下载全文 ===
                    ToolCall ragToolCall = ToolCall.builder()
                            .toolName("ragRetrieval").displayName("深度检索")
                            .icon("🔍").parameters("正在下载文章全文...").status("calling").build();
                    sink.next(ChatStreamEvent.toolCalling(sessionId, ragToolCall));

                    retrievedChunks = retrieveArticleById(detectedArticleId, userMessage);

                    if (retrievedChunks.isEmpty()) {
                        ragToolCall.setStatus("no_result");
                        ragToolCall.setParameters("文章内容获取失败");
                        sink.next(ChatStreamEvent.toolResult(sessionId, ragToolCall, null));
                    } else {
                        ragToolCall.setStatus("success");
                        ragToolCall.setResultCount(retrievedChunks.size());
                        ragToolCall.setParameters("已获取文章《" + retrievedChunks.get(0).title + "》全文");
                        sink.next(ChatStreamEvent.toolResult(sessionId, ragToolCall, null));
                    }
                } else {
                    // === 常规 RAG 模式：HyDE + 混合召回，带真实进度 ===

                    // Step 1: HyDE 生成假设文档
                    ToolCall ragToolCall = ToolCall.builder()
                            .toolName("ragRetrieval").displayName("深度检索")
                            .icon("🔍").parameters("正在生成 HyDE 假设文档...").status("calling").build();
                    sink.next(ChatStreamEvent.toolCalling(sessionId, ragToolCall));

                    String hydeDoc = generateHyDE(userMessage);
                    log.info("HyDE 假设文档生成完成，长度: {} 字符", hydeDoc.length());

                    // Step 2: 向量检索
                    ragToolCall.setParameters("正在向量检索 Top-K 相关段落...");
                    sink.next(ChatStreamEvent.toolCalling(sessionId, ragToolCall));

                    List<RetrievedArticle> vectorResults = new ArrayList<>();
                    try {
                        TextSegment hydeSegment = TextSegment.from(hydeDoc);
                        Embedding hydeEmbedding = embeddingModel.embed(hydeSegment).content();
                        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                                .queryEmbedding(hydeEmbedding).maxResults(RAG_TOP_K).minScore(0.4).build();
                        EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
                        vectorResults = searchResult.matches().stream().map(match -> {
                            RetrievedArticle a = new RetrievedArticle();
                            a.content = match.embedded().text();
                            a.score = match.score();
                            if (match.embedded().metadata() != null) {
                                a.articleId = match.embedded().metadata().getString("articleId");
                                a.title = match.embedded().metadata().getString("title");
                                a.authorName = match.embedded().metadata().getString("authorName");
                            }
                            return a;
                        }).collect(Collectors.toList());
                        log.info("HyDE 向量召回: {} 个结果", vectorResults.size());
                    } catch (Exception e) {
                        log.warn("HyDE 向量召回失败: {}", e.getMessage());
                    }

                    // Step 3: 关键词召回
                    ragToolCall.setParameters("正在关键词召回 + RRF 融合排序...");
                    sink.next(ChatStreamEvent.toolCalling(sessionId, ragToolCall));

                    List<RetrievedArticle> keywordResults = new ArrayList<>();
                    try {
                        String keyword = extractKeyword(userMessage);
                        if (!keyword.isEmpty()) {
                            List<Article> dbArticles = articleMapper.searchByKeyword(keyword, BM25_TOP_K);
                            for (int i = 0; i < dbArticles.size(); i++) {
                                Article a = dbArticles.get(i);
                                RetrievedArticle ra = new RetrievedArticle();
                                ra.articleId = a.getId(); ra.title = a.getTitle();
                                ra.content = (a.getTitle() != null ? a.getTitle() : "") + "\n" +
                                             (a.getSummary() != null ? a.getSummary() : "");
                                ra.score = 0.8 - (i * 0.05);
                                keywordResults.add(ra);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("关键词召回失败: {}", e.getMessage());
                    }

                    retrievedChunks = mergeWithRRF(vectorResults, keywordResults, 5);

                    // Step 4: 完成检索
                    if (retrievedChunks.isEmpty()) {
                        ragToolCall.setStatus("no_result");
                        ragToolCall.setParameters("未找到相关内容");
                        sink.next(ChatStreamEvent.toolResult(sessionId, ragToolCall, null));
                    } else {
                        ragToolCall.setStatus("success");
                        ragToolCall.setResultCount(retrievedChunks.size());
                        ragToolCall.setParameters(retrievedChunks.size() + " 条结果");
                        List<RecommendItem> recommendations = new ArrayList<>();
                        retrievedChunks.stream()
                                .collect(Collectors.toMap(
                                        a -> a.articleId != null ? a.articleId : "",
                                        a -> a, (a1, a2) -> a1.score > a2.score ? a1 : a2))
                                .values().forEach(a -> recommendations.add(RecommendItem.builder()
                                        .id(a.articleId).title(a.title).type("article")
                                        .rating(a.score * 100).build()));
                        sink.next(ChatStreamEvent.toolResult(sessionId, ragToolCall, recommendations));
                    }
                }

                // 5. 获取流式模型
                OpenAiStreamingChatModel streamingModel = getOrCreateStreamingModel(model, temperature, maxTokens);

                // 6. 构建带RAG上下文的消息列表
                boolean isFullTextMode = (detectedArticleId != null && !retrievedChunks.isEmpty());
                List<ChatMessage> messages = buildRAGMessages(request, retrievedChunks, isFullTextMode);

                // 7. 执行流式生成
                AtomicInteger tokenCount = new AtomicInteger(0);
                StringBuilder fullResponse = new StringBuilder();

                streamingModel.generate(messages, new StreamingResponseHandler<AiMessage>() {
                    @Override
                    public void onNext(String token) {
                        if (token != null && !token.isEmpty()) {
                            fullResponse.append(token);
                            tokenCount.incrementAndGet();
                            sink.next(ChatStreamEvent.message(token, sessionId));
                        }
                    }
                    @Override
                    public void onComplete(Response<AiMessage> response) {
                        log.info("RAG对话完成 - sessionId: {}, 总字符数: {}", sessionId, fullResponse.length());
                        ChatStreamEvent.TokenUsage usage = ChatStreamEvent.TokenUsage.builder()
                                .outputTokens(tokenCount.get()).build();
                        sink.next(ChatStreamEvent.done(sessionId, usage));
                        sink.complete();
                    }
                    @Override
                    public void onError(Throwable error) {
                        log.error("RAG对话出错 - sessionId: {}, error: {}", sessionId, error.getMessage());
                        sink.next(ChatStreamEvent.error(parseErrorMessage(error), sessionId));
                        sink.complete();
                    }
                });

            } catch (Exception e) {
                log.error("RAG对话失败: {}", e.getMessage(), e);
                sink.next(ChatStreamEvent.error("RAG服务异常: " + e.getMessage(), sessionId));
                sink.complete();
            }
        }, FluxSink.OverflowStrategy.BUFFER);
    }

    /**
     * 从用户消息中提取文章ID（UUID格式）
     */
    private String extractArticleId(String message) {
        if (message == null) return null;
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(message);
        if (matcher.find()) {
            String id = matcher.group();
            // 验证是否真的是文章ID
            Article article = articleMapper.getById(id);
            return article != null ? id : null;
        }
        return null;
    }

    /**
     * 通过文章ID获取全文内容（优先 Redis 缓存，未命中则下载并缓存）
     * 使用 synchronized 保证同一篇文章只有第一个请求下载，后续直接读缓存
     */
    private List<RetrievedArticle> retrieveArticleById(String articleId, String userQuery) {
        List<RetrievedArticle> results = new ArrayList<>();
        try {
            Article article = articleMapper.getById(articleId);
            if (article == null || article.getContentUrl() == null) return results;

            String redisKey = RedisConstant.ARTICLE_FULLTEXT + articleId;
            String content = null;

            // 1. 优先从 Redis 读取缓存
            Object cached = redisService.get(redisKey);
            if (cached != null && !cached.toString().trim().isEmpty()) {
                content = cached.toString();
                log.info("文章全文命中 Redis 缓存: articleId={}, length={}", articleId, content.length());
            } else {
                // 2. 未命中，synchronized 保证只有一个线程下载
                synchronized (articleId.intern()) {
                    // double-check：拿锁后再查一次缓存，防止重复下载
                    Object doubleCheck = redisService.get(redisKey);
                    if (doubleCheck != null && !doubleCheck.toString().trim().isEmpty()) {
                        content = doubleCheck.toString();
                        log.info("文章全文 double-check 命中缓存: articleId={}", articleId);
                    } else {
                        // 3. 从 OSS 下载
                        content = ossContentService.downloadMarkdownContent(article.getContentUrl());
                        if (content != null && !content.trim().isEmpty()) {
                            // 4. 存入 Redis，TTL 2小时
                            redisService.set(redisKey, content, RedisConstant.EXPIRE_2_HOURS);
                            log.info("文章全文已下载并缓存到 Redis: articleId={}, length={}", articleId, content.length());
                        }
                    }
                }
            }

            if (content == null || content.trim().isEmpty()) return results;

            // 全文过长时截断
            if (content.length() > 6000) {
                content = content.substring(0, 6000) + "\n\n...(内容已截断)";
            }

            RetrievedArticle ra = new RetrievedArticle();
            ra.articleId = articleId;
            ra.title = article.getTitle();
            ra.content = content;
            ra.score = 1.0;
            results.add(ra);

            log.info("文章全文获取成功: title={}, length={}", article.getTitle(), content.length());
        } catch (Exception e) {
            log.error("获取文章全文失败: articleId={}", articleId, e);
        }
        return results;
    }

    /**
     * HyDE + 多路召回检索
     *
     * 路1（语义）: 用 HyDE 生成假设文档 → Embedding → ChromaDB Top-K
     * 路2（关键词）: 从问题提取关键词 → MySQL LIKE 搜索 → 取标题+摘要
     * 合并: RRF（Reciprocal Rank Fusion）排序 → 取 Top-5
     */
    private List<RetrievedArticle> retrieveWithHyDE(String query) {
        try {
            // === 路1：HyDE 语义召回 ===
            List<RetrievedArticle> vectorResults = new ArrayList<>();
            try {
                // 生成假设性文档
                String hydeDoc = generateHyDE(query);
                log.info("HyDE 假设文档生成完成，长度: {} 字符", hydeDoc.length());

                // 用假设文档做向量检索
                TextSegment hydeSegment = TextSegment.from(hydeDoc);
                Embedding hydeEmbedding = embeddingModel.embed(hydeSegment).content();

                EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                        .queryEmbedding(hydeEmbedding)
                        .maxResults(RAG_TOP_K)
                        .minScore(0.4)
                        .build();
                EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);

                vectorResults = searchResult.matches().stream()
                        .map(match -> {
                            RetrievedArticle article = new RetrievedArticle();
                            article.content = match.embedded().text();
                            article.score = match.score();
                            if (match.embedded().metadata() != null) {
                                article.articleId = match.embedded().metadata().getString("articleId");
                                article.title = match.embedded().metadata().getString("title");
                                article.authorName = match.embedded().metadata().getString("authorName");
                            }
                            return article;
                        })
                        .collect(Collectors.toList());

                log.info("HyDE 向量召回: {} 个结果", vectorResults.size());
            } catch (Exception e) {
                log.warn("HyDE 向量召回失败，降级为原始查询: {}", e.getMessage());
                // 降级：直接用原始问题做向量检索
                vectorResults = retrieveRelevantArticles(query);
            }

            // === 路2：MySQL 关键词召回 ===
            List<RetrievedArticle> keywordResults = new ArrayList<>();
            try {
                String keyword = extractKeyword(query);
                if (!keyword.isEmpty()) {
                    List<Article> dbArticles = articleMapper.searchByKeyword(keyword, BM25_TOP_K);
                    for (int i = 0; i < dbArticles.size(); i++) {
                        Article a = dbArticles.get(i);
                        RetrievedArticle ra = new RetrievedArticle();
                        ra.articleId = a.getId();
                        ra.title = a.getTitle();
                        ra.content = (a.getTitle() != null ? a.getTitle() : "") + "\n" +
                                     (a.getSummary() != null ? a.getSummary() : "");
                        ra.score = 0.8 - (i * 0.05); // 按排名递减分数
                        keywordResults.add(ra);
                    }
                    log.info("关键词召回: {} 个结果, keyword={}", keywordResults.size(), keyword);
                }
            } catch (Exception e) {
                log.warn("关键词召回失败: {}", e.getMessage());
            }

            // === RRF 合并去重 ===
            return mergeWithRRF(vectorResults, keywordResults, 5);

        } catch (Exception e) {
            log.error("HyDE 混合检索失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * HyDE: 调用 LLM 生成假设性答案文档
     * 使用 qwen-turbo 快速生成，低成本
     */
    private String generateHyDE(String query) {
        try {
            OpenAiChatModel hydeModel = syncModelCache.computeIfAbsent("hyde_qwen-turbo", k ->
                    OpenAiChatModel.builder()
                            .apiKey(apiKey)
                            .baseUrl(baseUrl)
                            .modelName("qwen-turbo")
                            .temperature(0.3)
                            .maxTokens(300)
                            .logRequests(false)
                            .logResponses(false)
                            .build());

            String prompt = String.format(HYDE_PROMPT, query);
            String result = hydeModel.generate(prompt);
            return result != null ? result : query;
        } catch (Exception e) {
            log.warn("HyDE 生成失败，使用原始查询: {}", e.getMessage());
            return query;
        }
    }

    /**
     * RRF（Reciprocal Rank Fusion）合并两路召回结果
     * score = sum(1 / (rank_i + 60)) 对每一路
     */
    private List<RetrievedArticle> mergeWithRRF(
            List<RetrievedArticle> vectorResults,
            List<RetrievedArticle> keywordResults,
            int topN) {

        // 用 content hash 去重，以向量结果优先
        java.util.Map<String, RetrievedArticle> articleMap = new java.util.LinkedHashMap<>();
        java.util.Map<String, Double> rrfScores = new java.util.HashMap<>();

        // 向量路按排名计分
        for (int i = 0; i < vectorResults.size(); i++) {
            RetrievedArticle a = vectorResults.get(i);
            String key = a.articleId != null ? a.articleId + "_" + a.content.hashCode() : String.valueOf(a.content.hashCode());
            articleMap.put(key, a);
            rrfScores.merge(key, 1.0 / (i + 60), Double::sum);
        }

        // 关键词路按排名计分
        for (int i = 0; i < keywordResults.size(); i++) {
            RetrievedArticle a = keywordResults.get(i);
            String key = a.articleId != null ? a.articleId + "_kw" : String.valueOf(a.content.hashCode());
            articleMap.putIfAbsent(key, a);
            rrfScores.merge(key, 1.0 / (i + 60), Double::sum);
        }

        // 按 RRF 分数排序，取 topN
        return rrfScores.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(topN)
                .map(entry -> {
                    RetrievedArticle article = articleMap.get(entry.getKey());
                    article.score = entry.getValue();
                    return article;
                })
                .collect(Collectors.toList());
    }

    /**
     * 原始向量检索（降级方案 / 直接查询）
     */
    private List<RetrievedArticle> retrieveRelevantArticles(String query) {
        try {
            TextSegment querySegment = TextSegment.from(query);
            Embedding queryEmbedding = embeddingModel.embed(querySegment).content();

            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(RAG_TOP_K)
                    .minScore(0.4)
                    .build();
            EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);

            return searchResult.matches().stream()
                    .map(match -> {
                        RetrievedArticle article = new RetrievedArticle();
                        article.content = match.embedded().text();
                        article.score = match.score();
                        if (match.embedded().metadata() != null) {
                            article.articleId = match.embedded().metadata().getString("articleId");
                            article.title = match.embedded().metadata().getString("title");
                            article.authorName = match.embedded().metadata().getString("authorName");
                        }
                        return article;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("向量检索失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 构建带RAG上下文的消息列表
     * @param isFullTextMode true=全文阅读模式（指定文章ID），false=检索片段模式
     */
    private List<ChatMessage> buildRAGMessages(ChatRequestDTO request, List<RetrievedArticle> articles, boolean isFullTextMode) {
        List<ChatMessage> messages = new ArrayList<>();

        // 1. 构建系统提示词（区分全文模式和检索模式）
        String systemPrompt;
        if (isFullTextMode && !articles.isEmpty()) {
            RetrievedArticle fullArticle = articles.get(0);
            String content = fullArticle.content != null ? fullArticle.content : "";
            systemPrompt = String.format(RAG_FULLTEXT_SYSTEM_PROMPT,
                    fullArticle.title != null ? fullArticle.title : "未知文章",
                    content);
            log.info("全文阅读模式 - 文章: {}, 内容长度: {} 字符", fullArticle.title, content.length());
        } else {
            String ragContext = buildRAGContext(articles);
            systemPrompt = String.format(RAG_SYSTEM_PROMPT, ragContext);
        }
        messages.add(SystemMessage.from(systemPrompt));
        
        // 2. 添加历史对话（如果有）
        String conversationId = request.getConversationId();
        if (conversationId != null && !conversationId.isEmpty()) {
            List<AIMessage> dbMessages = messageMapper.getRecentMessages(conversationId, MAX_CONTEXT_MESSAGES);
            for (AIMessage dbMsg : dbMessages) {
                if ("user".equalsIgnoreCase(dbMsg.getRole())) {
                    messages.add(UserMessage.from(dbMsg.getContent()));
                } else if ("assistant".equalsIgnoreCase(dbMsg.getRole())) {
                    messages.add(AiMessage.from(dbMsg.getContent()));
                }
            }
        }
        
        // 3. 添加当前用户消息
        messages.add(UserMessage.from(request.getMessage()));
        
        log.debug("RAG消息列表构建完成，共 {} 条消息", messages.size());
        return messages;
    }

    /**
     * 构建RAG检索结果上下文（适配 chunk 段落）
     */
    private String buildRAGContext(List<RetrievedArticle> articles) {
        if (articles.isEmpty()) {
            return "（知识库中未找到相关参考资料。请直接基于你的通用专业知识回答，但不得编造任何平台文章、课程或教程。如果无法回答，坦诚告知。）";
        }

        StringBuilder context = new StringBuilder();
        for (int i = 0; i < articles.size(); i++) {
            RetrievedArticle article = articles.get(i);
            context.append(String.format("\n--- 参考段落 %d (来源: %s) ---\n",
                    i + 1, article.title != null ? article.title : "未知文章"));
            if (article.authorName != null) {
                context.append("作者: ").append(article.authorName).append("\n");
            }
            context.append("内容:\n");
            String content = article.content;
            if (content != null && content.length() > 1500) {
                content = content.substring(0, 1500) + "...";
            }
            context.append(content).append("\n");
        }
        return context.toString();
    }

    /**
     * 检索到的文章/段落内部类
     */
    @lombok.Data
    private static class RetrievedArticle {
        private String articleId;
        private String title;
        private String authorName;
        private String content;
        private double score;
    }
}
