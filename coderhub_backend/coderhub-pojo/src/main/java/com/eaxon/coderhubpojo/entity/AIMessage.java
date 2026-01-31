package com.eaxon.coderhubpojo.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI对话消息实体
 * 
 * @author CoderHub
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 消息ID（UUID） */
    private String id;

    /** 会话ID */
    private String conversationId;

    /** 角色：user-用户 assistant-AI system-系统 */
    private String role;

    /** 消息内容（Markdown格式） */
    private String content;

    /** 消息token数 */
    private Integer tokenCount;

    /** 工具调用信息（JSON） */
    private String toolCalls;

    /** 推荐内容列表（JSON） */
    private String recommendations;

    /** 生成此消息的模型 */
    private String model;

    /** 生成耗时（毫秒） */
    private Integer durationMs;

    /** 是否为错误消息 */
    private Boolean isError;

    /** 创建时间 */
    private LocalDateTime createdAt;
}

