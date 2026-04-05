package com.chenliang.chat.aiservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

/**
 * AI 助手控制器。
 * 展示了如何使用高层 AI 服务（AiService）进行交互。
 * 包含同步（普通字符串）和异步（流式片段）两种交互方式。
 */
@RestController
public class AssistantController {

    private final Assistant assistant;
    private final StreamingAssistant streamingAssistant;

    /**
     * 构造函数注入 AI 助手 Bean。
     * @param assistant 同步 AI 助手
     * @param streamingAssistant 流式 AI 助手
     */
    public AssistantController(Assistant assistant, StreamingAssistant streamingAssistant) {
        this.assistant = assistant;
        this.streamingAssistant = streamingAssistant;
    }

    /**
     * 同步聊天端点。
     * 调用 AI 模型，直到生成完整回复后一次性返回。
     * 
     * @param userId 用户唯一标识，用于区分对话上下文
     * @param message 用户消息，默认为基础问候
     * @return AI 的完整回复文本
     */
    @GetMapping("/assistant")
    public String assistant(
            @RequestParam(value = "userId", defaultValue = "user123") String userId,
            @RequestParam(value = "message", defaultValue = "你好") String message) {
        return assistant.chat(userId, message);
    }

    /**
     * 流式聊天端点。
     * 采用 SSE (Server-Sent Events) 技术，随着模型生成实时推送文本片段。
     * 
     * @param userId 用户唯一标识，用于区分对话上下文
     * @param message 用户消息，默认为基础问候
     * @return 响应式文本片段流
     */
    @GetMapping(value = "/streamingAssistant", produces = TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamingAssistant(
            @RequestParam(value = "userId", defaultValue = "user123") String userId,
            @RequestParam(value = "message", defaultValue = "你好") String message) {
        return streamingAssistant.chat(userId, message);
    }
}