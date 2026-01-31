package com.eaxon.coderhubserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import lombok.extern.slf4j.Slf4j;

/**
 * WebFlux CORS 配置
 * 专门处理响应式端点（SSE流式响应）的跨域问题
 * 
 * 注意：这个配置与 WebMvcConfiguration 中的 CORS 配置是互补的
 * - WebMvcConfiguration 处理传统的 Servlet 请求
 * - 这个配置处理 WebFlux 响应式请求
 * 
 * @author CoderHub
 */
@Configuration
@Slf4j
public class WebFluxCorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        log.info("配置 WebFlux CORS 过滤器（用于SSE流式响应）");
        
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许的前端地址
        config.addAllowedOriginPattern("http://localhost:*");
        config.addAllowedOriginPattern("http://127.0.0.1:*");
        
        // 允许所有请求方法
        config.addAllowedMethod("*");
        
        // 允许所有请求头
        config.addAllowedHeader("*");
        
        // 暴露响应头（SSE需要）
        config.addExposedHeader("Content-Type");
        config.addExposedHeader("Cache-Control");
        config.addExposedHeader("Connection");
        config.addExposedHeader("X-Accel-Buffering");
        
        // 允许携带凭证
        config.setAllowCredentials(true);
        
        // 预检请求缓存时间
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 只对 AI 相关接口应用此配置
        source.registerCorsConfiguration("/ai/**", config);
        
        log.info("WebFlux CORS 过滤器配置完成");
        return new CorsWebFilter(source);
    }
}

