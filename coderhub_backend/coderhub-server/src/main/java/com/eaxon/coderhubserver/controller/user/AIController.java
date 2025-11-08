package com.eaxon.coderhubserver.controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eaxon.coderhubcommon.result.Result;
import com.eaxon.coderhubserver.service.AIService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * AI 智能体控制器
 */
@RestController
@RequestMapping("/ai")
@Slf4j
@Api(tags = "AI智能体接口")
public class AIController {

    @Autowired
    private AIService aiService;

    /**
     * 流式对话接口 - SSE (Server-Sent Events)
     * 使用 EventSource 前端可以实时接收 AI 响应
     *
     * @param message 用户消息
     * @return Flux<String> 流式响应
     */
    @GetMapping(value = "/stream/chat", produces = "text/event-stream;charset=UTF-8")
    @ApiOperation("流式对话接口（SSE）")
    public Flux<String> streamChat(@RequestParam("message") String message) {
        log.info("收到流式对话请求，消息: {}", message);
        return aiService.streamChatSSE(message);
    }

    /**
     * 普通对话接口测试（非流式）
     *
     * @param message 用户消息
     * @return Result
     */
    @GetMapping("/chat")
    @ApiOperation("普通对话接口（测试用）")
    public Result<String> chat(@RequestParam("message") String message) {
        log.info("收到普通对话请求，消息: {}", message);
        return Result.success("接口正常，请使用 /ai/stream/chat 进行流式对话");
    }
}

