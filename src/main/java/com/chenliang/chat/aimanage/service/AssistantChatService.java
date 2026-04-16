package com.chenliang.chat.aimanage.service;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.chenliang.chat.aimanage.aiservice.DefaultAssistant;
import com.chenliang.chat.aimanage.aiservice.DictAssistant;
import com.chenliang.chat.aimanage.prompt.PromptService;
import com.chenliang.chat.entity.ChatSessionEntity;
import com.chenliang.chat.service.ChatSessionService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AssistantChatService {

    public static final String ASSISTANT_TYPE_DEFAULT = "default";
    public static final String ASSISTANT_TYPE_DICT = "dict";

    private static final String MEMORY_ID_SEPARATOR = "::";
    private static final String DEFAULT_SESSION_NAME = "新会话";
    private static final String PROMPT_MODE_DICT = "DICT_EXPERT";
    private static final String PROMPT_MODE_DEFAULT = "DEFAULT";

    private final DefaultAssistant defaultAssistant;
    private final DictAssistant dictAssistant;
    private final PromptService promptService;
    private final ChatSessionService chatSessionService;
    private final ChatMemoryStore chatMemoryStore;

    public AssistantChatService(DefaultAssistant defaultAssistant,
                                DictAssistant dictAssistant,
                                PromptService promptService,
                                ChatSessionService chatSessionService,
                                ChatMemoryStore chatMemoryStore) {
        this.defaultAssistant = defaultAssistant;
        this.dictAssistant = dictAssistant;
        this.promptService = promptService;
        this.chatSessionService = chatSessionService;
        this.chatMemoryStore = chatMemoryStore;
    }

    public Flux<String> streamingAssistant(String userId,
                                           Long sessionId,
                                           String message,
                                           String assistantType,
                                           String legacyIntent) {
        Long finalSessionId = getOrCreateSessionId(userId, sessionId);
        String resolvedAssistantType = normalizeAssistantType(assistantType, legacyIntent);
        String systemMessage = resolveSystemMessage(resolvedAssistantType);
        String memoryId = buildMemoryId(finalSessionId, resolvedAssistantType);

        if (ASSISTANT_TYPE_DICT.equals(resolvedAssistantType)) {
            return dictAssistant.chat(memoryId, systemMessage, message);
        }
        return defaultAssistant.chat(memoryId, systemMessage, message);
    }

    public List<Map<String, Object>> getSessionMessages(Long sessionId, String assistantType) {
        List<ChatMessage> messages = chatMemoryStore.getMessages(buildMemoryId(sessionId, assistantType));
        List<Map<String, Object>> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(messages)) {
            return result;
        }

        messages.stream()
                .filter(message -> !(message instanceof SystemMessage))
                .forEach(message -> appendMessage(result, message));
        return result;
    }

    private void appendMessage(List<Map<String, Object>> result, ChatMessage message) {
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
    }

    private String resolveSystemMessage(String assistantType) {
        if (ASSISTANT_TYPE_DICT.equals(assistantType)) {
            return promptService.getSystemPrompt(PROMPT_MODE_DICT);
        }
        return promptService.getSystemPrompt(PROMPT_MODE_DEFAULT);
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

        ChatSessionEntity newSession = chatSessionService.createSession(userId, DEFAULT_SESSION_NAME);
        return newSession.getId();
    }
}
