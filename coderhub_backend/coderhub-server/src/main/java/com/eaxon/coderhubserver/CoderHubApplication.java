package com.eaxon.coderhubserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableTransactionManagement
@EnableSwagger2  // 启用Swagger2
@ComponentScan(basePackages = "com.eaxon")
@Slf4j
public class CoderHubApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoderHubApplication.class, args);
        log.info("server:started successfully");
    }
}
