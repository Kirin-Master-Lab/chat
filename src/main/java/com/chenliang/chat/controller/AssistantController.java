package com.chenliang.chat.controller;

import com.chenliang.chat.aiservice.Assistant;
import com.chenliang.chat.aiservice.StreamingAssistant;
import com.chenliang.chat.aiservice.intent.IntentResolver;
import com.chenliang.chat.aiservice.prompt.PromptService;
import com.chenliang.chat.aiservice.state.StateStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

/**
 * AI 助手控制器。
 * 展示了如何使用高层 AI 服务（AiService）进行交互。
 * 已重构为使用解耦的服务化架构处理 SystemMessage。
 */
@RestController
public class AssistantController {

    private static final String STATE_MODE_KEY = "CURRENT_MODE";

    private final Assistant assistant;
    private final StreamingAssistant streamingAssistant;
    private final IntentResolver intentResolver;
    private final PromptService promptService;
    private final StateStore stateStore;

    public AssistantController(Assistant assistant, 
                               StreamingAssistant streamingAssistant,
                               IntentResolver intentResolver,
                               PromptService promptService,
                               StateStore stateStore) {
        this.assistant = assistant;
        this.streamingAssistant = streamingAssistant;
        this.intentResolver = intentResolver;
        this.promptService = promptService;
        this.stateStore = stateStore;
    }

    /**
     * 同步聊天端点。
     */
    @GetMapping("/assistant")
    public String assistant(
            @RequestParam(value = "userId", defaultValue = "user123") String userId,
            @RequestParam(value = "message", defaultValue = "你好") String message) {
        
        String systemMessage = resolveSystemMessage(userId, message);
        return assistant.chat(userId, systemMessage, message);
    }

    /**
     * 流式聊天端点。
     */
    @GetMapping(value = "/streamingAssistant", produces = TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamingAssistant(
            @RequestParam(value = "userId", defaultValue = "user123") String userId,
            @RequestParam(value = "message", defaultValue = "你好") String message) {
        
        String systemMessage = resolveSystemMessage(userId, message);
        return streamingAssistant.chat(userId, systemMessage, message);
    }

    /**
     * 核心逻辑重构：
     * 1. 从分布式状态存储中获取当前模式。
     * 2. 调用意图识别引擎确定最终意图。
     * 3. 将新状态存回分布式存储。
     * 4. 从提示词服务获取对应 Prompt。
     */
    private String resolveSystemMessage(String userId, String message) {
        // 1. 获取之前的状态 (从 Redis)
        Object currentModeObj = stateStore.get(userId, STATE_MODE_KEY);
        String currentMode = currentModeObj != null ? currentModeObj.toString() : null;

        // 2. 识别意图
        String finalIntent = intentResolver.resolveIntent(userId, message, currentMode);

        // 3. 更新状态 (实现状态流转)
        if (IntentResolver.MODE_DEFAULT.equals(finalIntent)) {
            stateStore.remove(userId, STATE_MODE_KEY);
        } else {
            stateStore.set(userId, STATE_MODE_KEY, finalIntent);
        }

        // 4. 获取并返回提示词
        return promptService.getSystemPrompt(finalIntent);
    }
}
