package com.eaxon.coderhubpojo.DTO;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 对话请求 DTO
 * 
 * @author CoderHub
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI对话请求")
public class ChatRequestDTO {

    @Schema(description = "用户消息内容", required = true, example = "请解释什么是微服务架构？")
    private String message;

    @Schema(description = "选择的AI模型", example = "qwen-plus")
    private String model;

    @Schema(description = "温度参数（0-1），控制回复的随机性", example = "0.7")
    private Double temperature;

    @Schema(description = "对话历史记录")
    private List<ChatMessage> history;

    @Schema(description = "会话ID，用于关联对话上下文")
    private String sessionId;

    @Schema(description = "系统提示词（可选）")
    private String systemPrompt;

    @Schema(description = "最大输出token数")
    private Integer maxTokens;

    /**
     * 对话消息（用于历史记录）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "对话消息")
    public static class ChatMessage {
        
        @Schema(description = "消息角色", example = "user", allowableValues = {"user", "assistant", "system"})
        private String role;

        @Schema(description = "消息内容")
        private String content;
    }
}

