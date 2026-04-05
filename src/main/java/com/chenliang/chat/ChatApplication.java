package com.chenliang.chat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 聊天机器人后端应用程序启动类。
 * 基于 Spring Boot 框架，集成了 LangChain4j 以提供 AI 服务能力。
 */
@SpringBootApplication
@MapperScan("com.chenliang.chat.mapper")
public class ChatApplication {

    /**
     * 应用程序入口点。
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);
    }

}
