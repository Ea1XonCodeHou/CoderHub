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

import lombok.extern.slf4j.Slf4j;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Web MVC配置类
 * 注册拦截器、配置Swagger等
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
     *
     * @param registry
     */
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册JWT拦截器...");
        
        // 注册管理员端拦截器
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")                   // 拦截所有/admin/**路径
                .excludePathPatterns("/admin/login")            // 排除管理员登录接口
                .excludePathPatterns("/common/**");             // 排除通用接口
        
        // 注册用户端拦截器
        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/user/**")                    // 拦截所有/user/**路径
                .addPathPatterns("/article/**")                 // 拦截所有/article/**路径
                .excludePathPatterns("/user/login")             // 排除登录接口
                .excludePathPatterns("/user/register")          // 排除注册接口
                .excludePathPatterns("/common/**");             // 排除通用接口（文件上传等）
        
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
                .allowedOrigins("http://localhost:5173", "http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
        
        log.info("CORS跨域配置完成");
    }

    /**
     * 扩展消息转换器，确保使用 UTF-8 编码（不覆盖默认转换器）
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("开始扩展消息转换器（UTF-8编码）...");
        
        // 在现有转换器前面添加 UTF-8 字符串转换器
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        stringConverter.setWriteAcceptCharset(false); // 禁止写入 Accept-Charset
        converters.add(0, stringConverter);
        
        log.info("消息转换器扩展完成");
    }

    /**
     * 通过Swagger生成接口文档
     *
     * @return
     */
    @Bean
    public Docket docket() {
        log.info("开始构建Swagger API文档...");
        
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("CoderHub技术博客平台接口文档")
                .version("1.0")
                .description("CoderHub后端API接口文档")
                .build();
        
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.eaxon.coderhubserver.controller"))
                .paths(PathSelectors.any())
                .build();
        
        log.info("Swagger API文档构建完成");
        return docket;
    }

    /**
     * 设置静态资源映射
     * 主要用于Swagger UI的静态资源访问
     *
     * @param registry
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始配置静态资源映射...");
        
        // Swagger UI 资源映射
        registry.addResourceHandler("/doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        
        // Swagger2 资源映射
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        
        log.info("静态资源映射配置完成");
    }
}

