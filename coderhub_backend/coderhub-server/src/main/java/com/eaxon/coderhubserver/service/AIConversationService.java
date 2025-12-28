package com.eaxon.coderhubserver.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eaxon.coderhubpojo.DTO.ConversationDTO;
import com.eaxon.coderhubpojo.DTO.ConversationDTO.MessageDTO;
import com.eaxon.coderhubpojo.entity.AIConversation;
import com.eaxon.coderhubpojo.entity.AIMessage;
import com.eaxon.coderhubserver.mapper.AIConversationMapper;
import com.eaxon.coderhubserver.mapper.AIMessageMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;

/**
 * AI会话服务层
 * 
 * @author CoderHub
 */
@Service
@Slf4j
public class AIConversationService {

    @Autowired
    private AIConversationMapper conversationMapper;

    @Autowired
    private AIMessageMapper messageMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${langchain4j.open-ai.streaming-chat-model.api-key}")
    private String apiKey;

    @Value("${langchain4j.open-ai.streaming-chat-model.base-url}")
    private String baseUrl;

    /** 上下文窗口最大消息数 */
    private static final int MAX_CONTEXT_MESSAGES = 20;

    // ==================== 会话管理 ====================

    /**
     * 创建新会话
     */
    @Transactional
    public ConversationDTO createConversation(String userId, String title, String model) {
        AIConversation conversation = AIConversation.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .title(title != null ? title : "新对话")
                .model(model != null ? model : "qwen-plus")
                .messageCount(0)
                .isPinned(false)
                .isArchived(false)
                .status(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        conversationMapper.insert(conversation);
        log.info("创建新会话: userId={}, conversationId={}", userId, conversation.getId());

        return toDTO(conversation);
    }

    /**
     * 获取用户的会话列表
     */
    public List<ConversationDTO> getUserConversations(String userId) {
        List<AIConversation> conversations = conversationMapper.getByUserId(userId, 1);
        return conversations.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取会话详情（包含消息）
     */
    public ConversationDTO getConversationWithMessages(String conversationId, String userId) {
        AIConversation conversation = conversationMapper.getById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            return null;
        }

        ConversationDTO dto = toDTO(conversation);
        
        // 加载消息
        List<AIMessage> messages = messageMapper.getByConversationId(conversationId);
        dto.setMessages(messages.stream()
                .map(this::toMessageDTO)
                .collect(Collectors.toList()));

        return dto;
    }

    /**
     * 更新会话
     */
    @Transactional
    public boolean updateConversation(String conversationId, String userId, 
                                       ConversationDTO.UpdateRequest request) {
        AIConversation conversation = conversationMapper.getById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            return false;
        }

        AIConversation update = AIConversation.builder()
                .id(conversationId)
                .title(request.getTitle())
                .isPinned(request.getIsPinned())
                .isArchived(request.getIsArchived())
                .build();

        conversationMapper.update(update);
        return true;
    }

    /**
     * 删除会话
     */
    @Transactional
    public boolean deleteConversation(String conversationId, String userId) {
        AIConversation conversation = conversationMapper.getById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            return false;
        }

        conversationMapper.softDelete(conversationId);
        log.info("删除会话: conversationId={}", conversationId);
        return true;
    }

    // ==================== 消息管理 ====================

    /**
     * 添加消息到会话
     */
    @Transactional
    public MessageDTO addMessage(String conversationId, String userId, 
                                  ConversationDTO.AddMessageRequest request) {
        // 验证会话归属
        AIConversation conversation = conversationMapper.getById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            return null;
        }

        // 创建消息
        AIMessage message = AIMessage.builder()
                .id(UUID.randomUUID().toString())
                .conversationId(conversationId)
                .role(request.getRole())
                .content(request.getContent())
                .tokenCount(request.getTokenCount())
                .toolCalls(request.getToolCalls())
                .recommendations(request.getRecommendations())
                .model(request.getModel())
                .durationMs(request.getDurationMs())
                .isError(false)
                .createdAt(LocalDateTime.now())
                .build();

        messageMapper.insert(message);

        // 更新会话信息
        Integer count = messageMapper.countByConversationId(conversationId);
        String preview = request.getContent();
        if (preview != null && preview.length() > 50) {
            preview = preview.substring(0, 50) + "...";
        }
        conversationMapper.updateMessageInfo(conversationId, count, preview);

        // 如果是第一条AI回复且标题为"新对话"，自动生成标题
        if ("assistant".equalsIgnoreCase(request.getRole()) 
            && "新对话".equals(conversation.getTitle())
            && count <= 2) {
            // 获取第一条用户消息
            List<AIMessage> messages = messageMapper.getByConversationId(conversationId);
            AIMessage firstUserMsg = messages.stream()
                    .filter(m -> "user".equalsIgnoreCase(m.getRole()))
                    .findFirst()
                    .orElse(null);
            
            if (firstUserMsg != null) {
                // 异步生成标题，不阻塞响应
                final String userContent = firstUserMsg.getContent();
                final String aiContent = request.getContent();
                new Thread(() -> generateTitle(conversationId, userContent, aiContent)).start();
            }
        }

        log.debug("添加消息: conversationId={}, role={}", conversationId, request.getRole());
        return toMessageDTO(message);
    }

    /**
     * 获取会话的消息列表
     */
    public List<MessageDTO> getMessages(String conversationId, String userId) {
        AIConversation conversation = conversationMapper.getById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            return new ArrayList<>();
        }

        List<AIMessage> messages = messageMapper.getByConversationId(conversationId);
        return messages.stream()
                .map(this::toMessageDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取上下文消息（用于AI对话）
     */
    public List<AIMessage> getContextMessages(String conversationId) {
        return messageMapper.getRecentMessages(conversationId, MAX_CONTEXT_MESSAGES);
    }

    // ==================== 标题生成 ====================

    /**
     * 自动生成会话标题
     */
    public void generateTitle(String conversationId, String userMessage, String aiResponse) {
        try {
            // 使用LLM生成标题
            OpenAiChatModel chatModel = OpenAiChatModel.builder()
                    .apiKey(apiKey)
                    .baseUrl(baseUrl)
                    .modelName("qwen-turbo")
                    .temperature(0.3)
                    .maxTokens(50)
                    .build();

            String prompt = String.format("""
                    基于以下对话内容，生成一个简短的中文标题（8-15个字，不要标点符号）：
                    
                    用户：%s
                    AI：%s
                    
                    只返回标题内容，不要任何解释。
                    """, 
                    userMessage.length() > 200 ? userMessage.substring(0, 200) : userMessage,
                    aiResponse.length() > 200 ? aiResponse.substring(0, 200) : aiResponse);

            String title = chatModel.generate(prompt).trim();
            
            // 清理标题
            title = title.replace("\"", "").replace("'", "").replace("\n", "");
            if (title.length() > 50) {
                title = title.substring(0, 50);
            }

            conversationMapper.updateTitle(conversationId, title);
            log.info("生成会话标题: conversationId={}, title={}", conversationId, title);

        } catch (Exception e) {
            log.error("生成标题失败: {}", e.getMessage());
            // 失败时使用用户消息作为标题
            String fallbackTitle = userMessage.length() > 20 ? 
                    userMessage.substring(0, 20) + "..." : userMessage;
            conversationMapper.updateTitle(conversationId, fallbackTitle);
        }
    }

    // ==================== 转换方法 ====================

    private ConversationDTO toDTO(AIConversation entity) {
        return ConversationDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .model(entity.getModel())
                .messageCount(entity.getMessageCount())
                .lastMessagePreview(entity.getLastMessagePreview())
                .isPinned(entity.getIsPinned())
                .isArchived(entity.getIsArchived())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private MessageDTO toMessageDTO(AIMessage entity) {
        MessageDTO dto = MessageDTO.builder()
                .id(entity.getId())
                .role(entity.getRole())
                .content(entity.getContent())
                .model(entity.getModel())
                .createdAt(entity.getCreatedAt())
                .build();

        // 解析JSON字段
        if (entity.getToolCalls() != null) {
            try {
                dto.setToolCalls(objectMapper.readValue(entity.getToolCalls(), Object.class));
            } catch (JsonProcessingException e) {
                log.warn("解析toolCalls失败: {}", e.getMessage());
            }
        }

        if (entity.getRecommendations() != null) {
            try {
                dto.setRecommendations(objectMapper.readValue(entity.getRecommendations(), Object.class));
            } catch (JsonProcessingException e) {
                log.warn("解析recommendations失败: {}", e.getMessage());
            }
        }

        return dto;
    }
}

