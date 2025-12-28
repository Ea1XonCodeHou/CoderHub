package com.eaxon.coderhubserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableTransactionManagement
@EnableScheduling  // 启用定时任务
@ComponentScan(basePackages = "com.eaxon")
@Slf4j
public class CoderHubApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoderHubApplication.class, args);
        log.info("server:started successfully");
    }
}
