package com.chenliang.chat.controller;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.chenliang.chat.aiservice.Assistant;
import com.chenliang.chat.aiservice.DictAssistant;
import com.chenliang.chat.aiservice.intent.IntentResolver;
import com.chenliang.chat.aiservice.prompt.PromptService;
import com.chenliang.chat.entity.ChatSessionEntity;
import com.chenliang.chat.service.ChatSessionService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
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

@RestController
public class AssistantController {

    private static final String ASSISTANT_TYPE_DEFAULT = "default";
    private static final String ASSISTANT_TYPE_DICT = "dict";
    private static final String MEMORY_ID_SEPARATOR = "::";

    private final Assistant assistant;
    private final DictAssistant dictAssistant;
    private final PromptService promptService;
    private final ChatSessionService chatSessionService;
    private final ChatMemoryStore chatMemoryStore;

    public AssistantController(Assistant assistant,
                               DictAssistant dictAssistant,
                               PromptService promptService,
                               ChatSessionService chatSessionService,
                               ChatMemoryStore chatMemoryStore) {
        this.assistant = assistant;
        this.dictAssistant = dictAssistant;
        this.promptService = promptService;
        this.chatSessionService = chatSessionService;
        this.chatMemoryStore = chatMemoryStore;
    }

    @GetMapping(value = "/streamingAssistant", produces = TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamingAssistant(
            @RequestParam(value = "userId", defaultValue = "user123") String userId,
            @RequestParam(value = "sessionId", required = false) Long sessionId,
            @RequestParam(value = "message", defaultValue = "你好") String message,
            @RequestParam(value = "assistantType", required = false) String assistantType,
            @RequestParam(value = "intent", required = false) String legacyIntent) {

        Long finalSessionId = getOrCreateSessionId(userId, sessionId);
        String resolvedAssistantType = normalizeAssistantType(assistantType, legacyIntent);
        String systemMessage = resolveSystemMessage(resolvedAssistantType);
        String memoryId = buildMemoryId(finalSessionId, resolvedAssistantType);

        if (ASSISTANT_TYPE_DICT.equals(resolvedAssistantType)) {
            return dictAssistant.chat(memoryId, systemMessage, message);
        }
        return assistant.chat(memoryId, systemMessage, message);
    }

    @GetMapping("/session/messages")
    public List<Map<String, Object>> getSessionMessages(
            @RequestParam Long sessionId,
            @RequestParam(value = "assistantType", defaultValue = ASSISTANT_TYPE_DEFAULT) String assistantType) {

        List<ChatMessage> messages = chatMemoryStore.getMessages(buildMemoryId(sessionId, assistantType));
        List<Map<String, Object>> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(messages)) {
            return result;
        }

        messages.stream()
                .filter(message -> !(message instanceof SystemMessage))
                .forEach(message -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("role", message.type().toString().toLowerCase());

                    if (message instanceof UserMessage userMessage) {
                        item.put("content", userMessage.singleText());
                        result.add(item);
                        return;
                    }

                    if (message instanceof AiMessage aiMessage) {
                        if (aiMessage.text() != null) {
                            item.put("content", aiMessage.text());
                            result.add(item);
                        }
                        return;
                    }

                    if (message instanceof ToolExecutionResultMessage toolMessage) {
                        item.put("content", toolMessage.text());
                        result.add(item);
                    }
                });

        return result;
    }

    private String resolveSystemMessage(String assistantType) {
        if (ASSISTANT_TYPE_DICT.equals(assistantType)) {
            return promptService.getSystemPrompt(IntentResolver.MODE_DICT);
        }
        return promptService.getSystemPrompt(IntentResolver.MODE_DEFAULT);
    }

    private String buildMemoryId(Long sessionId, String assistantType) {
        return sessionId + MEMORY_ID_SEPARATOR + normalizeAssistantType(assistantType, null);
    }

    private String normalizeAssistantType(String assistantType, String legacyIntent) {
        String candidate = assistantType;
        if (candidate == null || candidate.trim().isEmpty()) {
            candidate = legacyIntent;
        }
        if (ASSISTANT_TYPE_DICT.equalsIgnoreCase(candidate)) {
            return ASSISTANT_TYPE_DICT;
        }
        return ASSISTANT_TYPE_DEFAULT;
    }

    private Long getOrCreateSessionId(String userId, Long sessionId) {
        if (sessionId != null) {
            ChatSessionEntity session = chatSessionService.getById(sessionId);
            if (session != null && userId.equals(session.getUserId())) {
                return sessionId;
            }
        }

        ChatSessionEntity defaultSession = chatSessionService.getDefaultSession(userId);
        if (defaultSession != null) {
            return defaultSession.getId();
        }

        ChatSessionEntity newSession = chatSessionService.createSession(userId, "新会话");
        return newSession.getId();
    }
}
