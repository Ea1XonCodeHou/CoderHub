package com.eaxon.coderhubserver.config;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import com.eaxon.coderhubserver.interceptor.JwtTokenAdminInterceptor;
import com.eaxon.coderhubserver.interceptor.JwtTokenUserInterceptor;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.extern.slf4j.Slf4j;

/**
 * Web MVC配置类
 * 注册拦截器、配置OpenAPI文档、CORS等
 * 
 * 升级到 Spring Boot 3.x，使用 SpringDoc OpenAPI 替代 Swagger2
 */
@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;
    
    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    /**
     * 注册自定义拦截器
     */
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册JWT拦截器...");
        
        // 注册管理员端拦截器
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login")
                .excludePathPatterns("/common/**");
        
        // 注册用户端拦截器
        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/user/**")
                .addPathPatterns("/article/**")
                .excludePathPatterns("/user/login")
                .excludePathPatterns("/user/register")
                .excludePathPatterns("/common/**");
        
        log.info("JWT拦截器注册完成");
    }

    /**
     * 配置异步支持（用于响应式流式输出）
     */
    @Override
    protected void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        log.info("开始配置异步任务执行器...");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-stream-");
        executor.initialize();
        
        configurer.setTaskExecutor(executor);
        configurer.setDefaultTimeout(180000); // 3分钟超时
        
        log.info("异步任务执行器配置完成");
    }

    /**
     * 配置 CORS（跨域资源共享）
     */
    @Override
    protected void addCorsMappings(CorsRegistry registry) {
        log.info("开始配置CORS跨域...");
        
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Content-Type", "Cache-Control", "Connection")
                .allowCredentials(true)
                .maxAge(3600);
        
        log.info("CORS跨域配置完成");
    }

    /**
     * 扩展消息转换器，确保使用 UTF-8 编码
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("开始扩展消息转换器（UTF-8编码）...");
        
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        stringConverter.setWriteAcceptCharset(false);
        converters.add(0, stringConverter);
        
        log.info("消息转换器扩展完成");
    }

    /**
     * 配置 OpenAPI 文档（替代 Swagger2 Docket）
     */
    @Bean
    public OpenAPI customOpenAPI() {
        log.info("开始构建OpenAPI文档...");
        
        return new OpenAPI()
                .info(new Info()
                        .title("CoderHub 技术博客平台接口文档")
                        .version("2.0")
                        .description("CoderHub 后端 API 接口文档 - 基于 Spring Boot 3.x")
                        .contact(new Contact()
                                .name("CoderHub Team")
                                .email("support@coderhub.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }

    /**
     * 设置静态资源映射
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始配置静态资源映射...");
        
        // SpringDoc OpenAPI UI 资源映射
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/");
        
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        
        log.info("静态资源映射配置完成");
    }
}
