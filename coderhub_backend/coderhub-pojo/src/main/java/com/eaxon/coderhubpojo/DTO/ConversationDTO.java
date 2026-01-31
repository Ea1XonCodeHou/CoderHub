package com.eaxon.coderhubpojo.DTO;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 会话DTO
 * 
 * @author CoderHub
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI会话信息")
public class ConversationDTO {

    @Schema(description = "会话ID")
    private String id;

    @Schema(description = "会话标题")
    private String title;

    @Schema(description = "使用的模型")
    private String model;

    @Schema(description = "消息数量")
    private Integer messageCount;

    @Schema(description = "最后一条消息预览")
    private String lastMessagePreview;

    @Schema(description = "是否置顶")
    private Boolean isPinned;

    @Schema(description = "是否归档")
    private Boolean isArchived;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    /**
     * 消息列表（仅在获取会话详情时返回）
     */
    @Schema(description = "消息列表")
    private List<MessageDTO> messages;

    /**
     * 消息DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "消息信息")
    public static class MessageDTO {

        @Schema(description = "消息ID")
        private String id;

        @Schema(description = "角色：user/assistant/system")
        private String role;

        @Schema(description = "消息内容")
        private String content;

        @Schema(description = "工具调用信息")
        private Object toolCalls;

        @Schema(description = "推荐内容")
        private Object recommendations;

        @Schema(description = "使用的模型")
        private String model;

        @Schema(description = "创建时间")
        private LocalDateTime createdAt;
    }

    /**
     * 创建会话请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "创建会话请求")
    public static class CreateRequest {

        @Schema(description = "会话标题（可选）")
        private String title;

        @Schema(description = "使用的模型")
        private String model;
    }

    /**
     * 更新会话请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "更新会话请求")
    public static class UpdateRequest {

        @Schema(description = "会话标题")
        private String title;

        @Schema(description = "是否置顶")
        private Boolean isPinned;

        @Schema(description = "是否归档")
        private Boolean isArchived;
    }

    /**
     * 添加消息请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "添加消息请求")
    public static class AddMessageRequest {

        @Schema(description = "角色：user/assistant", required = true)
        private String role;

        @Schema(description = "消息内容", required = true)
        private String content;

        @Schema(description = "工具调用信息（JSON字符串）")
        private String toolCalls;

        @Schema(description = "推荐内容（JSON字符串）")
        private String recommendations;

        @Schema(description = "使用的模型")
        private String model;

        @Schema(description = "token数")
        private Integer tokenCount;

        @Schema(description = "生成耗时")
        private Integer durationMs;
    }
}

