package com.chenliang.chat.config;

import com.chenliang.chat.aiservice.Assistant;
import com.chenliang.chat.listener.MyChatModelListener;
import com.chenliang.chat.aiservice.StreamingAssistant;
import com.chenliang.chat.lowlevel.ChatModelController;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * AI 助手配置类。
 * 负责定义 AI 服务所需的聊天记忆、模型监听器等核心组件。
 */
@Configuration
public class AssistantConfiguration {

    /**
     * 定义 RestTemplate Bean，用于发送 HTTP 请求。
     * 
     * @return 默认的 RestTemplate 实例
     */
    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * 定义聊天记忆提供者。
     * 该组件将为每个不同的 memoryId (即 userId) 提供一个独立的聊天记忆实例。
     * <p>
     * 通过关联 {@link CustomChatMemoryStore}，实现了对话记忆持久化到 MySQL。
     *
     * @param customChatMemoryStore 自定义的持久化存储组件
     * @return 聊天记忆提供者，默认保存最近 10 条消息以维护对话上下文。
     */
    @Bean
    ChatMemoryProvider chatMemoryProvider(CustomChatMemoryStore customChatMemoryStore) {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(50)
                .chatMemoryStore(customChatMemoryStore)
                .build();
    }

    /**
     * 定义模型监听器。
     * 该监听器会自动注入到 Spring 上下文中找到的所有 {@link ChatModel} 和 {@link StreamingChatModel} Bean 中。
     * <p>
     * 它不仅能监控 {@link Assistant} 和 {@link StreamingAssistant} 的调用，
     * 还能监控 {@link ChatModelController} 中直接使用的底层模型交互。
     *
     * @return 自定义的聊天模型监听器实例，用于记录请求、响应和错误。
     */
    @Bean
    ChatModelListener chatModelListener() {
        return new MyChatModelListener();
    }
}
