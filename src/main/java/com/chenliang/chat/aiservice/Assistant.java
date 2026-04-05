package com.chenliang.chat.aiservice;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;

/**
 * 声明式 AI 助手服务接口。
 * 使用 LangChain4j 的 {@link AiService} 注解，Spring 会在运行时自动创建此接口的实现类。
 * 它集成了大语言模型、聊天记忆和工具调用等能力。
 */
@AiService
public interface Assistant {

    /**
     * 发送用户消息并获取 AI 的回复。
     * 配置了系统消息引导 AI 的语气。
     * 
     * @param userId 用户唯一标识，用于区分对话上下文 (会话隔离)
     * @param userMessage 用户输入的文本
     * @return AI 生成的响应文本
     */
    @SystemMessage("你是一个公司业务助手，请用专业且礼貌的态度回答问题。")
    String chat(@MemoryId String userId, String userMessage);
}