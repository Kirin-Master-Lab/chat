package com.chenliang.chat.controller;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.chenliang.chat.aiservice.Assistant;
import com.chenliang.chat.aiservice.DictAssistant;
import com.chenliang.chat.aiservice.intent.IntentResolver;
import com.chenliang.chat.aiservice.prompt.PromptService;
import com.chenliang.chat.aiservice.state.StateStore;
import com.chenliang.chat.entity.ChatSessionEntity;
import com.chenliang.chat.service.ChatSessionService;
import dev.langchain4j.data.message.*;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final DictAssistant dictAssistant;
    private final IntentResolver intentResolver;
    private final PromptService promptService;
    private final StateStore stateStore;
    private final ChatSessionService chatSessionService;
    private final ChatMemoryStore chatMemoryStore;

    public AssistantController(Assistant assistant,
                               DictAssistant dictAssistant,
                               IntentResolver intentResolver,
                               PromptService promptService,
                               StateStore stateStore,
                               ChatSessionService chatSessionService,
                               ChatMemoryStore chatMemoryStore) {
        this.assistant = assistant;
        this.dictAssistant = dictAssistant;
        this.intentResolver = intentResolver;
        this.promptService = promptService;
        this.stateStore = stateStore;
        this.chatSessionService = chatSessionService;
        this.chatMemoryStore = chatMemoryStore;
    }

//    /**
//     * 同步聊天端点。
//     */
//    @GetMapping("/assistant")
//    public String assistant(
//            @RequestParam(value = "userId", defaultValue = "user123") String userId,
//            @RequestParam(value = "sessionId", required = false) Long sessionId,
//            @RequestParam(value = "message", defaultValue = "你好") String message) {
//
//        // 处理会话逻辑
//        Long finalSessionId = getOrCreateSessionId(userId, sessionId);
//
//        String systemMessage = resolveSystemMessage(userId, message);
//        return assistant.chat(finalSessionId, systemMessage, message);
//    }

    /**
     * 流式聊天端点。
     */
    @GetMapping(value = "/streamingAssistant", produces = TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamingAssistant(
            @RequestParam(value = "userId", defaultValue = "user123") String userId,
            @RequestParam(value = "sessionId", required = false) Long sessionId,
            @RequestParam(value = "message", defaultValue = "你好") String message) {

        // 处理会话逻辑
        Long finalSessionId = getOrCreateSessionId(userId, sessionId);

        String systemMessage = resolveSystemMessage(userId, message);
        return dictAssistant.chat(finalSessionId, systemMessage, message);
    }

    /**
     * 获取会话历史消息
     */
    @GetMapping("/session/messages")
    public List<Map<String, Object>> getSessionMessages(@RequestParam Long sessionId) {
        List<ChatMessage> messages = chatMemoryStore.getMessages(sessionId);
        List<Map<String, Object>> result = new ArrayList<>();

        if (CollectionUtils.isEmpty(messages)) {
            return result;
        }
        messages.stream().filter(k -> !(k instanceof SystemMessage)).forEach(k -> {

            Map<String, Object> item = new HashMap<>();
            item.put("role", k.type().toString().toLowerCase());
            String content = null;
            if (k instanceof SystemMessage) {
                SystemMessage systemMessage = (SystemMessage) k;
                content = systemMessage.text();
                // 过滤空内容的系统消息
                if (content == null || content.trim().isEmpty()) {
                    return;
                }
                item.put("content", content);
            } else if (k instanceof UserMessage) {
                UserMessage userMessage = (UserMessage) k;
                content = userMessage.singleText();
                item.put("content", content);
            } else if (k instanceof AiMessage) {
                AiMessage aiMessage = (AiMessage) k;
                content = aiMessage.text();
                // 过滤content为null的AI消息
                if (content == null) {
                    return;
                }
                item.put("content", content);
            } else if (k instanceof ToolExecutionResultMessage) {
                ToolExecutionResultMessage toolExecutionResultMessage = (ToolExecutionResultMessage) k;
                content = toolExecutionResultMessage.text();
                item.put("content", content);
            }

            result.add(item);
        });


//        for (ChatMessage message : messages) {
//            Map<String, Object> item = new HashMap<>();
//            item.put("role", message.type().toString().toLowerCase());
//            item.put("content", ((SystemMessage) message).text());
//            result.add(item);
//        }
        return result;
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

    /**
     * 获取或创建会话ID
     */
    private Long getOrCreateSessionId(String userId, Long sessionId) {
        if (sessionId != null) {
            // 验证会话是否属于当前用户
            ChatSessionEntity session = chatSessionService.getById(sessionId);
            if (session != null && userId.equals(session.getUserId())) {
                return sessionId;
            }
        }
        // 没有sessionId或者验证失败，获取默认会话
        ChatSessionEntity defaultSession = chatSessionService.getDefaultSession(userId);
        if (defaultSession != null) {
            return defaultSession.getId();
        }
        // 没有默认会话，创建新会话
        ChatSessionEntity newSession = chatSessionService.createSession(userId, "新会话");
        return newSession.getId();
    }
}
