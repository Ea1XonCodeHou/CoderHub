package com.eaxon.coderhubserver.controller.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eaxon.coderhubcommon.context.BaseContext;
import com.eaxon.coderhubcommon.result.Result;
import com.eaxon.coderhubpojo.DTO.ConversationDTO;
import com.eaxon.coderhubpojo.DTO.ConversationDTO.MessageDTO;
import com.eaxon.coderhubserver.service.AIConversationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * AI会话管理控制器
 * 
 * @author CoderHub
 */
@RestController
@RequestMapping("/ai/conversations")
@Slf4j
@Tag(name = "AI会话管理接口", description = "会话的增删改查、消息管理")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173"}, allowCredentials = "true")
public class AIConversationController {

    @Autowired
    private AIConversationService conversationService;

    // ==================== 会话管理 ====================

    /**
     * 获取用户的会话列表
     */
    @GetMapping
    @Operation(summary = "获取会话列表", description = "获取当前用户的所有会话")
    public Result<List<ConversationDTO>> getConversations() {
        String userId = getCurrentUserId();
        log.info("获取会话列表: userId={}", userId);
        
        List<ConversationDTO> conversations = conversationService.getUserConversations(userId);
        return Result.success(conversations);
    }

    /**
     * 创建新会话
     */
    @PostMapping
    @Operation(summary = "创建会话", description = "创建新的对话会话")
    public Result<ConversationDTO> createConversation(@RequestBody(required = false) ConversationDTO.CreateRequest request) {
        String userId = getCurrentUserId();
        log.info("创建会话: userId={}", userId);
        
        String title = request != null ? request.getTitle() : null;
        String model = request != null ? request.getModel() : null;
        
        ConversationDTO conversation = conversationService.createConversation(userId, title, model);
        return Result.success(conversation);
    }

    /**
     * 获取会话详情（包含消息）
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取会话详情", description = "获取会话信息及其所有消息")
    public Result<ConversationDTO> getConversation(@PathVariable("id") String conversationId) {
        String userId = getCurrentUserId();
        log.info("获取会话详情: conversationId={}", conversationId);
        
        ConversationDTO conversation = conversationService.getConversationWithMessages(conversationId, userId);
        if (conversation == null) {
            return Result.error("会话不存在或无权访问");
        }
        return Result.success(conversation);
    }

    /**
     * 更新会话
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新会话", description = "更新会话标题、置顶状态等")
    public Result<String> updateConversation(
            @PathVariable("id") String conversationId,
            @RequestBody ConversationDTO.UpdateRequest request) {
        String userId = getCurrentUserId();
        log.info("更新会话: conversationId={}", conversationId);
        
        boolean success = conversationService.updateConversation(conversationId, userId, request);
        if (!success) {
            return Result.error("更新失败，会话不存在或无权操作");
        }
        return Result.success("更新成功");
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除会话", description = "软删除会话")
    public Result<String> deleteConversation(@PathVariable("id") String conversationId) {
        String userId = getCurrentUserId();
        log.info("删除会话: conversationId={}", conversationId);
        
        boolean success = conversationService.deleteConversation(conversationId, userId);
        if (!success) {
            return Result.error("删除失败，会话不存在或无权操作");
        }
        return Result.success("删除成功");
    }

    // ==================== 消息管理 ====================

    /**
     * 获取会话的消息列表
     */
    @GetMapping("/{id}/messages")
    @Operation(summary = "获取消息列表", description = "获取会话的所有消息")
    public Result<List<MessageDTO>> getMessages(@PathVariable("id") String conversationId) {
        String userId = getCurrentUserId();
        log.info("获取消息列表: conversationId={}", conversationId);
        
        List<MessageDTO> messages = conversationService.getMessages(conversationId, userId);
        return Result.success(messages);
    }

    /**
     * 添加消息到会话
     */
    @PostMapping("/{id}/messages")
    @Operation(summary = "添加消息", description = "向会话添加一条消息")
    public Result<MessageDTO> addMessage(
            @PathVariable("id") String conversationId,
            @RequestBody ConversationDTO.AddMessageRequest request) {
        String userId = getCurrentUserId();
        log.debug("添加消息: conversationId={}, role={}", conversationId, request.getRole());
        
        MessageDTO message = conversationService.addMessage(conversationId, userId, request);
        if (message == null) {
            return Result.error("添加失败，会话不存在或无权操作");
        }
        return Result.success(message);
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取当前用户ID
     * 优先从JWT中获取，如果未登录则使用固定的测试用户ID
     */
    private String getCurrentUserId() {
        try {
            String userId = BaseContext.getCurrentId();
            log.debug("从BaseContext获取用户ID: {}", userId);
            if (userId != null && !userId.isEmpty()) {
                return userId;
            }
        } catch (Exception e) {
            log.debug("从BaseContext获取用户ID失败: {}", e.getMessage());
        }
        
        // 未登录时使用固定的测试用户ID（确保一致性）
        // 使用固定ID而不是"anonymous"，保证创建和添加消息时ID一致
        String defaultId = "test-user-001";
        log.debug("使用默认用户ID: {}", defaultId);
        return defaultId;
    }
}

