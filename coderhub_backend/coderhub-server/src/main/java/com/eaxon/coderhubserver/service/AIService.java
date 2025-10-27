package com.eaxon.coderhubserver.service;

/**
 * AI 智能体服务接口
 */
public interface AIService {
    
    /**
     * 简单对话
     * @param message 用户消息
     * @return AI 回复
     */
    String chat(String message);
}

