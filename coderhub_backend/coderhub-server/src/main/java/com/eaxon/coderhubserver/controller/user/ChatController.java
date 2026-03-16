package com.eaxon.coderhubserver.controller.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eaxon.coderhubcommon.context.BaseContext;
import com.eaxon.coderhubcommon.result.Result;
import com.eaxon.coderhubpojo.VO.ChatConversationVO;
import com.eaxon.coderhubpojo.VO.ChatMsgVO;
import com.eaxon.coderhubserver.websocket.ChatWebSocketHandler;
import com.eaxon.coderhubserver.service.ChatService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * 私信聊天 REST Controller
 * WebSocket 消息收发由 ChatWebSocketHandler 处理
 */
@RestController
@RequestMapping("/chat")
@Tag(name = "私信聊天")
@Slf4j
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * 获取当前用户的会话列表
     */
    @GetMapping("/conversations")
    @Operation(summary = "获取会话列表")
    public Result<List<ChatConversationVO>> getConversations() {
        String userId = BaseContext.getCurrentId();
        return Result.success(chatService.getConversations(userId));
    }

    /**
     * 获取或创建与指定用户的会话，返回 conversationId
     */
    @PostMapping("/conversations/with/{targetUserId}")
    @Operation(summary = "获取或创建会话")
    public Result<String> getOrCreateConversation(@PathVariable String targetUserId) {
        String currentUserId = BaseContext.getCurrentId();
        String convId = chatService.getOrCreateConversationId(currentUserId, targetUserId);
        return Result.success(convId);
    }

    /**
     * 获取会话历史消息（分页，每页50条）
     */
    @GetMapping("/conversations/{convId}/messages")
    @Operation(summary = "获取历史消息")
    public Result<List<ChatMsgVO>> getMessages(
            @PathVariable String convId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pageSize) {
        String userId = BaseContext.getCurrentId();
        return Result.success(chatService.getMessages(convId, userId, page, pageSize));
    }

    /**
     * 标记会话消息为已读
     */
    @PostMapping("/conversations/{convId}/read")
    @Operation(summary = "标记已读")
    public Result<Void> markAsRead(@PathVariable String convId) {
        String userId = BaseContext.getCurrentId();
        chatService.markAsRead(convId, userId);
        return Result.success();
    }

    /**
     * 获取私信未读总数（供导航栏角标使用）
     */
    @GetMapping("/unread-count")
    @Operation(summary = "获取未读消息数")
    public Result<Integer> getUnreadCount() {
        String userId = BaseContext.getCurrentId();
        return Result.success(chatService.getUnreadCount(userId));
    }

    /**
     * 查询指定用户是否在线
     */
    @GetMapping("/user/{userId}/online")
    @Operation(summary = "查询用户在线状态")
    public Result<Boolean> isUserOnline(@PathVariable String userId) {
        return Result.success(ChatWebSocketHandler.isUserOnline(userId));
    }
}
