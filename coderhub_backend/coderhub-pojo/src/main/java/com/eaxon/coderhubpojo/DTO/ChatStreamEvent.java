package com.eaxon.coderhubpojo.DTO;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 对话流式响应事件
 * 用于 SSE 流式输出
 * 
 * @author CoderHub
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI对话流式响应事件")
public class ChatStreamEvent {

    /**
     * 事件类型枚举
     */
    public enum EventType {
        /** 思考中 */
        THINKING,
        /** 工具调用中 */
        TOOL_CALLING,
        /** 工具调用完成 */
        TOOL_RESULT,
        /** 内容片段 */
        MESSAGE,
        /** 完成 */
        DONE,
        /** 错误 */
        ERROR
    }

    @Schema(description = "事件类型", example = "MESSAGE")
    private EventType type;

    @Schema(description = "内容片段（当类型为MESSAGE时）")
    private String content;

    @Schema(description = "会话ID")
    private String sessionId;

    @Schema(description = "使用的模型名称")
    private String model;

    @Schema(description = "错误信息（当类型为ERROR时）")
    private String error;

    @Schema(description = "token使用情况")
    private TokenUsage tokenUsage;

    @Schema(description = "工具调用信息（当类型为TOOL_CALLING/TOOL_RESULT时）")
    private ToolCall toolCall;

    @Schema(description = "推荐内容列表（当有推荐结果时）")
    private List<RecommendItem> recommendations;

    /**
     * Token 使用情况
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Token使用情况")
    public static class TokenUsage {
        @Schema(description = "输入token数")
        private Integer inputTokens;

        @Schema(description = "输出token数")
        private Integer outputTokens;

        @Schema(description = "总token数")
        private Integer totalTokens;
    }

    /**
     * 工具调用信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "工具调用信息")
    public static class ToolCall {
        @Schema(description = "工具名称", example = "searchTutorials")
        private String toolName;

        @Schema(description = "工具显示名称", example = "搜索教程")
        private String displayName;

        @Schema(description = "工具图标", example = "📚")
        private String icon;

        @Schema(description = "调用参数描述", example = "关键词: Spring Boot")
        private String parameters;

        @Schema(description = "工具状态: calling/success/failed")
        private String status;

        @Schema(description = "结果数量")
        private Integer resultCount;
        
        @Schema(description = "工具返回的JSON结果（用于前端解析渲染）")
        private String toolResult;
    }

    /**
     * 推荐内容项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "推荐内容项")
    public static class RecommendItem {
        @Schema(description = "内容ID")
        private String id;

        @Schema(description = "内容类型: tutorial/article")
        private String type;

        @Schema(description = "标题")
        private String title;

        @Schema(description = "描述/摘要")
        private String description;

        @Schema(description = "封面图URL")
        private String coverImage;

        @Schema(description = "作者/讲师名称")
        private String author;

        @Schema(description = "浏览量/学习人数")
        private Long viewCount;

        @Schema(description = "评分")
        private Double rating;

        @Schema(description = "标签列表")
        private List<String> tags;

        @Schema(description = "跳转链接")
        private String link;
    }

    // =============== 静态工厂方法 ===============

    /**
     * 创建思考中事件
     */
    public static ChatStreamEvent thinking(String sessionId, String model) {
        return ChatStreamEvent.builder()
                .type(EventType.THINKING)
                .sessionId(sessionId)
                .model(model)
                .build();
    }

    /**
     * 创建工具调用中事件
     */
    public static ChatStreamEvent toolCalling(String sessionId, ToolCall toolCall) {
        return ChatStreamEvent.builder()
                .type(EventType.TOOL_CALLING)
                .sessionId(sessionId)
                .toolCall(toolCall)
                .build();
    }

    /**
     * 创建工具调用结果事件
     */
    public static ChatStreamEvent toolResult(String sessionId, ToolCall toolCall, List<RecommendItem> recommendations) {
        return ChatStreamEvent.builder()
                .type(EventType.TOOL_RESULT)
                .sessionId(sessionId)
                .toolCall(toolCall)
                .recommendations(recommendations)
                .build();
    }

    /**
     * 创建内容片段事件
     */
    public static ChatStreamEvent message(String content, String sessionId) {
        return ChatStreamEvent.builder()
                .type(EventType.MESSAGE)
                .content(content)
                .sessionId(sessionId)
                .build();
    }

    /**
     * 创建完成事件
     */
    public static ChatStreamEvent done(String sessionId, TokenUsage usage) {
        return ChatStreamEvent.builder()
                .type(EventType.DONE)
                .sessionId(sessionId)
                .tokenUsage(usage)
                .build();
    }

    /**
     * 创建完成事件（带推荐内容）
     */
    public static ChatStreamEvent doneWithRecommendations(String sessionId, TokenUsage usage, List<RecommendItem> recommendations) {
        return ChatStreamEvent.builder()
                .type(EventType.DONE)
                .sessionId(sessionId)
                .tokenUsage(usage)
                .recommendations(recommendations)
                .build();
    }

    /**
     * 创建错误事件
     */
    public static ChatStreamEvent error(String errorMessage, String sessionId) {
        return ChatStreamEvent.builder()
                .type(EventType.ERROR)
                .error(errorMessage)
                .sessionId(sessionId)
                .build();
    }
}

