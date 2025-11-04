package com.eaxon.coderhubserver.task;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.eaxon.coderhubserver.mapper.ArticleMapper;
import com.eaxon.coderhubserver.service.RedisService;

import lombok.extern.slf4j.Slf4j;

/**
 * 文章浏览量同步定时任务
 * 每5分钟将Redis中的浏览量同步到MySQL
 */
@Component
@Slf4j
public class ArticleViewCountSyncTask {
    
    @Autowired
    private RedisService redisService;
    
    @Autowired
    private ArticleMapper articleMapper;
    
    /**
     * 同步Redis浏览量到MySQL
     * 每5分钟执行一次
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void syncViewCountToDatabase() {
        log.info("========== 开始同步浏览量到数据库 ==========");
        
        long startTime = System.currentTimeMillis();
        int successCount = 0;
        int failCount = 0;
        
        try {
            // 1. 扫描Redis获取所有浏览量数据
            Map<String, Long> viewCountMap = redisService.scanAllViewCounts();
            
            if (viewCountMap.isEmpty()) {
                log.info("Redis中没有浏览量数据需要同步");
                return;
            }
            
            log.info("扫描到 {} 篇文章的浏览量数据", viewCountMap.size());
            
            // 2. 批量更新到MySQL
            for (Map.Entry<String, Long> entry : viewCountMap.entrySet()) {
                String articleId = entry.getKey();
                Long viewCount = entry.getValue();
                
                try {
                    // 直接覆盖MySQL中的浏览量
                    articleMapper.updateViewCountDirect(articleId, viewCount);
                    successCount++;
                    
                    log.debug("同步成功：articleId={}, viewCount={}", articleId, viewCount);
                } catch (Exception e) {
                    failCount++;
                    log.error("同步失败：articleId={}, viewCount={}", articleId, viewCount, e);
                }
            }
            
            long endTime = System.currentTimeMillis();
            long costTime = endTime - startTime;
            
            log.info("========== 浏览量同步完成 ==========");
            log.info("总数: {}, 成功: {}, 失败: {}, 耗时: {}ms", 
                    viewCountMap.size(), successCount, failCount, costTime);
            
        } catch (Exception e) {
            log.error("浏览量同步任务执行失败", e);
        }
    }
}
