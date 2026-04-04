package com.chenliang.chat.aiservice;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

/**
 * 声明式流式 AI 助手服务接口。
 * 使用 LangChain4j 的 {@link AiService} 注解，提供异步流式响应能力。
 * 适合在 Web 端实现逐字显示的打字机效果。
 */
@AiService
public interface StreamingAssistant {

    /**
     * 发送用户消息并以响应式流的形式获取 AI 的回复。
     * 配置了系统消息引导 AI 的语气。
     * 
     * @param userMessage 用户输入的文本
     * @return 包含 AI 生成文本片段的异步流 (Flux)
     */
    @SystemMessage("You are a polite assistant")
    Flux<String> chat(String userMessage);
}
