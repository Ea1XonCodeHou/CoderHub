package com.eaxon.coderhubserver.controller.admin;

import com.eaxon.coderhubcommon.constant.RedisConstant;
import com.eaxon.coderhubcommon.result.Result;
import com.eaxon.coderhubserver.mapper.UserMapper;
import com.eaxon.coderhubserver.service.RedisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 管理端 - AI 提问额度管理接口
 * 支持批量初始化现有普通用户额度、查询单用户额度、手动修改额度
 */
@RestController
@RequestMapping("/admin/ai-quota")
@Slf4j
@Tag(name = "管理端 - AI额度管理", description = "批量初始化用户 AI 提问额度等管理操作")
public class AIQuotaManagerController {

    @Autowired
    private RedisService redisService;

    @Autowired
    private UserMapper userMapper;

    /**
     * 批量初始化所有普通用户的 AI 提问额度
     * 已有记录的用户不会被覆盖；只为 Redis 中尚无记录的用户写入初始值
     */
    @PostMapping("/init-all")
    @Operation(summary = "批量初始化全部普通用户 AI 额度",
               description = "为所有状态正常的普通用户（role=USER）在 Redis 中初始化免费提问额度，已有记录的跳过")
    public Result<Map<String, Object>> initAllUsersQuota(
            @RequestBody(required = false) InitRequest req) {

        int quota = (req != null && req.getQuota() != null && req.getQuota() > 0)
                ? req.getQuota()
                : RedisConstant.AI_QUOTA_FREE;

        log.info("管理员触发批量初始化 AI 额度，目标额度: {}", quota);

        List<String> userIds = userMapper.getAllActiveUserIds();
        AtomicInteger initialized = new AtomicInteger(0);
        AtomicInteger skipped = new AtomicInteger(0);

        for (String userId : userIds) {
            Boolean success = redisService.initAiQuota(userId, quota);
            if (Boolean.TRUE.equals(success)) {
                initialized.incrementAndGet();
            } else {
                skipped.incrementAndGet();
            }
        }

        log.info("批量初始化完成：初始化 {} 人，跳过（已有额度）{} 人", initialized.get(), skipped.get());

        Map<String, Object> result = new HashMap<>();
        result.put("total", userIds.size());
        result.put("initialized", initialized.get());
        result.put("skipped", skipped.get());
        result.put("quota", quota);
        return Result.success(result);
    }

    /**
     * 查询指定用户的 AI 提问剩余额度
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "查询指定用户的 AI 额度")
    public Result<Map<String, Object>> getUserQuota(@PathVariable String userId) {
        Integer quota = redisService.getAiQuota(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("quota", quota);
        data.put("exists", quota != null);
        return Result.success(data);
    }

    /**
     * 手动设置指定用户的 AI 额度（覆盖）
     */
    @PutMapping("/user/{userId}")
    @Operation(summary = "手动设置指定用户的 AI 额度")
    public Result<String> setUserQuota(@PathVariable String userId,
                                       @RequestBody SetQuotaRequest req) {
        if (req.getQuota() == null || req.getQuota() < 0) {
            return Result.error("额度值不合法");
        }
        redisService.setAiQuota(userId, req.getQuota());
        log.info("管理员手动设置用户 {} 额度为 {}", userId, req.getQuota());
        return Result.success("设置成功");
    }

    @Data
    static class InitRequest {
        /** 自定义初始额度，不传则使用默认值 10 */
        private Integer quota;
    }

    @Data
    static class SetQuotaRequest {
        private Integer quota;
    }
}
