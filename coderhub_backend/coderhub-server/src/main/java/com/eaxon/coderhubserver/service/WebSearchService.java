package com.eaxon.coderhubserver.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Tavily 联网搜索服务
 * 调用 Tavily Search API，为 RAG 流程补充互联网实时内容
 */
@Service
@Slf4j
public class WebSearchService {

    @Value("${coder.tavily.api-key:}")
    private String apiKey;

    private static final String TAVILY_API_URL = "https://api.tavily.com/search";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 执行联网搜索
     *
     * @param query      搜索关键词
     * @param maxResults 最大返回结果数
     * @return 搜索结果列表
     */
    public List<WebSearchResult> search(String query, int maxResults) {
        List<WebSearchResult> results = new ArrayList<>();

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Tavily API Key 未配置，跳过联网搜索");
            return results;
        }

        try {
            String requestJson = objectMapper.writeValueAsString(new java.util.HashMap<>() {{
                put("api_key", apiKey);
                put("query", query);
                put("search_depth", "basic");
                put("include_answer", false);
                put("max_results", maxResults);
            }});

            RequestBody body = RequestBody.create(requestJson, JSON);
            Request request = new Request.Builder()
                    .url(TAVILY_API_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.warn("Tavily 搜索失败，HTTP状态: {}", response.code());
                    return results;
                }

                String responseBody = response.body().string();
                JsonNode root = objectMapper.readTree(responseBody);
                JsonNode items = root.path("results");

                if (items.isArray()) {
                    for (JsonNode item : items) {
                        WebSearchResult result = new WebSearchResult();
                        result.setTitle(item.path("title").asText(""));
                        result.setUrl(item.path("url").asText(""));
                        result.setContent(item.path("content").asText(""));
                        result.setScore(item.path("score").asDouble(0.5));
                        result.setSiteName(extractSiteName(result.getUrl()));
                        results.add(result);
                    }
                }

                log.info("Tavily 联网搜索完成，query='{}', 返回 {} 条结果", query, results.size());
            }
        } catch (Exception e) {
            log.error("Tavily 联网搜索异常: {}", e.getMessage(), e);
        }

        return results;
    }

    /**
     * 从 URL 中提取站点域名
     */
    private String extractSiteName(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host == null) return url;
            // 去掉 www. 前缀
            return host.startsWith("www.") ? host.substring(4) : host;
        } catch (Exception e) {
            return url;
        }
    }

    /**
     * 联网搜索结果
     */
    @Data
    public static class WebSearchResult {
        private String title;
        private String url;
        private String content;
        private String siteName;
        private double score;
    }
}
