package com.eaxon.coderhubserver.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.eaxon.coderhubpojo.entity.Article;
import com.eaxon.coderhubserver.mapper.ArticleMapper;
import com.eaxon.coderhubserver.service.RedisService;

import lombok.extern.slf4j.Slf4j;

/**
 * Redis缓存预热任务
 * 服务启动时，将热门文章的浏览量加载到Redis
 */
@Component
@Slf4j
public class RedisWarmUpTask {
    
    @Autowired
    private ArticleMapper articleMapper;
    
    @Autowired
    private RedisService redisService;
    
    /**
     * 服务启动时执行预热
     * 加载点赞数最多的前100篇文章的浏览量到Redis
     */
    @PostConstruct
    public void warmUpViewCounts() {
        log.info("========== 开始Redis缓存预热 ==========");
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 查询点赞数最多的前100篇文章
            List<Article> hotArticles = articleMapper.getTopLikedArticles(100);
            
            if (hotArticles == null || hotArticles.isEmpty()) {
                log.info("没有文章数据需要预热");
                return;
            }
            
            log.info("查询到 {} 篇热门文章", hotArticles.size());
            
            // 2. 构建浏览量映射
            Map<String, Long> viewCountMap = new HashMap<>();
            for (Article article : hotArticles) {
                viewCountMap.put(article.getId(), article.getViewCount());
            }
            
            // 3. 批量写入Redis
            redisService.batchSetViewCount(viewCountMap);
            
            long endTime = System.currentTimeMillis();
            long costTime = endTime - startTime;
            
            log.info("========== Redis缓存预热完成 ==========");
            log.info("预热文章数: {}, 耗时: {}ms", hotArticles.size(), costTime);
            
            // 4. 打印前10篇热门文章信息
            log.info("预热的热门文章TOP10:");
            int count = Math.min(10, hotArticles.size());
            for (int i = 0; i < count; i++) {
                Article article = hotArticles.get(i);
                log.info("  {}. {} (点赞:{}, 浏览:{})", 
                        i + 1, 
                        article.getTitle(), 
                        article.getLikeCount(), 
                        article.getViewCount());
            }
            
        } catch (Exception e) {
            log.error("Redis缓存预热失败", e);
        }
    }
}
