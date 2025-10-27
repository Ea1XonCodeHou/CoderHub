package com.eaxon.coderhubserver.controller.user;

import com.eaxon.coderhubcommon.result.Result;
import com.eaxon.coderhubserver.service.AIService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
     * 简单对话测试
     */
    @PostMapping("/chat")
    @ApiOperation("AI对话")
    public Result<String> chat(@RequestParam String message) {
        log.info("AI对话请求：message={}", message);
        String response = aiService.chat(message);
        return Result.success(response);
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    @ApiOperation("健康检查")
    public Result<String> health() {
        return Result.success("AI服务运行正常");
    }
}

