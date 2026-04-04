package com.chenliang.chat.aiservice;

import com.chenliang.chat.lowlevel.ChatModelController;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * AI 助手配置类。
 * 负责定义 AI 服务所需的聊天记忆、模型监听器等核心组件。
 */
@Configuration
public class AssistantConfiguration {

    /**
     * 定义聊天记忆组件。
     * 该组件将被 {@link Assistant} 和 {@link StreamingAssistant} 自动使用。
     * 
     * 使用 {@code SCOPE_PROTOTYPE} (多例模式) 是为了确保每次注入时都会创建一个新的实例。
     * 这是实现用户隔离对话（基于 userId）的关键，
     * 每次调用 AI Service 时，LangChain4j 会根据当前会话需求获取一个新的 ChatMemory 实例。
     * 
     * @return 默认使用消息窗口聊天记忆，保存最近 10 条消息以维护对话上下文。
     */
    @Bean
    @Scope(SCOPE_PROTOTYPE)
    ChatMemory chatMemory() {
        return MessageWindowChatMemory.withMaxMessages(10);
    }

    /**
     * 定义模型监听器。
     * 该监听器会自动注入到 Spring 上下文中找到的所有 {@link ChatModel} 和 {@link StreamingChatModel} Bean 中。
     * 
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
