package com.eaxon.coderhubserver.controller.admin;

import com.eaxon.coderhubcommon.result.Result;
import com.eaxon.coderhubserver.mapper.UserMapper;
import com.eaxon.coderhubserver.service.impl.NotificationBroadcastService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端 - 通知管理接口
 * 支持向全体用户广播系统通知（异步批量发送，接口立即返回）
 */
@RestController
@RequestMapping("/admin/notification")
@Slf4j
@Tag(name = "管理端-通知管理接口")
public class NotificationManagerController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationBroadcastService broadcastService;

    /**
     * 广播系统通知给所有用户
     * 接口立即返回，后台异步逐条投递 MQ 消息
     */
    @PostMapping("/broadcast")
    @Operation(summary = "广播系统通知给全体用户")
    public Result<BroadcastResult> broadcast(@RequestBody BroadcastRequest req) {
        if (req.getContent() == null || req.getContent().isBlank()) {
            return Result.error("通知内容不能为空");
        }

        // 查询所有正常状态用户的 ID
        List<String> userIds = userMapper.getAllActiveUserIds();
        int total = userIds.size();

        log.info("广播系统通知，共 {} 名用户目标，内容：{}", total, req.getContent());

        // 异步批量投递，不阻塞接口响应
        broadcastService.asyncBroadcast(userIds, req);

        return Result.success(new BroadcastResult(total, "通知已提交，正在后台发送中"));
    }

    // ==================== 内部 DTO / VO ====================

    @Data
    public static class BroadcastRequest {
        /** 可选 emoji 前缀，如 "📢" */
        private String emoji;
        /** 通知正文 */
        private String content;
    }

    @Data
    public static class BroadcastResult {
        private int targetCount;
        private String message;

        public BroadcastResult(int targetCount, String message) {
            this.targetCount = targetCount;
            this.message = message;
        }
    }
}
