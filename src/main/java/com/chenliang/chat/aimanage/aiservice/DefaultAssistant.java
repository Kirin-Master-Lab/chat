package com.chenliang.chat.aimanage.aiservice;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

/**
 * 声明式 AI 助手服务接口。
 * 使用 LangChain4j 的 {@link AiService} 注解，Spring 会在运行时自动创建此接口的实现类。
 * 它集成了大语言模型、聊天记忆和工具调用等能力。
 * 显式挂载 DictionaryTools 以确保字典管理功能正常工作。
 */
@AiService
public interface DefaultAssistant {

    /**
     * 发送用户消息并获取 AI 的回复。
     *
     * @param systemMessage 动态系统提示词
     * @param userMessage   用户输入的文本
     * @return AI 生成的响应文本
     */
    @SystemMessage("{{systemMessage}}")
    Flux<String> chat(@MemoryId String memoryId,
                      @dev.langchain4j.service.V("systemMessage") String systemMessage,
                      @UserMessage String userMessage);
}
