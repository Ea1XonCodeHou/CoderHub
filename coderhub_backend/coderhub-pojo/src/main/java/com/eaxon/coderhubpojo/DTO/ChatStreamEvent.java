package com.eaxon.coderhubpojo.DTO;

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

