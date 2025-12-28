package com.eaxon.coderhubpojo.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI对话会话实体
 * 
 * @author CoderHub
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIConversation implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 会话ID（UUID） */
    private String id;

    /** 用户ID */
    private String userId;

    /** 会话标题 */
    private String title;

    /** 使用的AI模型 */
    private String model;

    /** 消息数量 */
    private Integer messageCount;

    /** 最后一条消息预览 */
    private String lastMessagePreview;

    /** 是否置顶 */
    private Boolean isPinned;

    /** 是否归档 */
    private Boolean isArchived;

    /** 状态：1-正常 0-已删除 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}

