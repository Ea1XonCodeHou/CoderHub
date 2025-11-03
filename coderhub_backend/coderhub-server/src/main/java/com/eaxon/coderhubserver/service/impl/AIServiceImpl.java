package com.eaxon.coderhubserver.service.impl;

import com.eaxon.coderhubserver.service.AIService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * AI 智能体服务实现
 */
@Service
@Slf4j
public class AIServiceImpl implements AIService {

    @Autowired
    private ChatLanguageModel chatLanguageModel;

    @Override
    public String chat(String message) {
        log.info("收到用户消息：{}", message);
        
        try {
            // 调用 AI 模型
            String response = chatLanguageModel.generate(message);
            log.info("AI 回复：{}", response);
            return response;
        } catch (Exception e) {
            log.error("AI 调用失败", e);
            throw new RuntimeException("AI 服务暂时不可用：" + e.getMessage());
        }
    }
}