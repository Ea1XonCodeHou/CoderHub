package com.eaxon.coderhubserver.service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * OSS内容下载服务
 * 负责从阿里云OSS或MinIO下载文章Markdown内容
 * 
 * @author CoderHub
 */
@Service
@Slf4j
public class OssContentService {

    private final OkHttpClient httpClient;
    
    public OssContentService() {
        // 配置HTTP客户端
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 下载OSS上的Markdown文件内容
     * 
     * @param ossUrl OSS文件URL（完整的HTTP地址）
     * @return Markdown文件内容
     */
    public String downloadMarkdownContent(String ossUrl) {
        if (ossUrl == null || ossUrl.isEmpty()) {
            log.warn("OSS URL为空，无法下载内容");
            return null;
        }
        
        log.info("开始下载OSS内容: {}", ossUrl);
        
        Request request = new Request.Builder()
                .url(ossUrl)
                .get()
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("下载OSS内容失败，HTTP状态码: {}", response.code());
                return null;
            }
            
            if (response.body() == null) {
                log.error("下载OSS内容失败，响应体为空");
                return null;
            }
            
            String content = response.body().string();
            log.info("OSS内容下载成功，长度: {} 字符", content.length());
            return content;
            
        } catch (IOException e) {
            log.error("下载OSS内容时发生IO异常: {}", e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("下载OSS内容时发生未知异常: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 批量下载多个文件内容
     * 
     * @param ossUrls OSS文件URL列表
     * @return 内容列表（与输入顺序一致，失败的位置为null）
     */
    public java.util.List<String> downloadMultipleContents(java.util.List<String> ossUrls) {
        return ossUrls.stream()
                .map(this::downloadMarkdownContent)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 检查URL是否可访问
     * 
     * @param ossUrl OSS文件URL
     * @return 是否可访问
     */
    public boolean isUrlAccessible(String ossUrl) {
        if (ossUrl == null || ossUrl.isEmpty()) {
            return false;
        }
        
        Request request = new Request.Builder()
                .url(ossUrl)
                .head() // 只请求头部，不下载内容
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            return response.isSuccessful();
        } catch (Exception e) {
            log.warn("检查URL可访问性失败: {}", ossUrl, e);
            return false;
        }
    }
}
