package com.eaxon.coderhubserver.config;

import com.eaxon.coderhubserver.interceptor.JwtTokenUserInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
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

    /**
     * 注册自定义拦截器
     *
     * @param registry
     */
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册JWT拦截器...");
        
        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/user/**")                    // 拦截所有/user/**路径
                .excludePathPatterns("/user/login")             // 排除登录接口
                .excludePathPatterns("/user/register");         // 排除注册接口
        
        log.info("JWT拦截器注册完成");
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

