package com.chenliang.chat.aimanage.service;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.chenliang.chat.aimanage.aiservice.DefaultAssistant;
import com.chenliang.chat.aimanage.aiservice.DictAssistant;
import com.chenliang.chat.aimanage.aiservice.MapAssistant;
import com.chenliang.chat.entity.AssistantInfoEntity;
import com.chenliang.chat.entity.ChatSessionEntity;
import com.chenliang.chat.service.AssistantInfoService;
import com.chenliang.chat.service.AssistantPromptTemplates;
import com.chenliang.chat.service.ChatSessionService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class AssistantChatService {

    private static final String MEMORY_ID_SEPARATOR = "::";
    private static final String DEFAULT_SESSION_NAME = "新会话";

    private final DefaultAssistant defaultAssistant;
    private final DictAssistant dictAssistant;
    private final MapAssistant mapAssistant;
    private final AssistantInfoService assistantInfoService;
    private final ChatSessionService chatSessionService;
    private final ChatMemoryStore chatMemoryStore;

    public AssistantChatService(DefaultAssistant defaultAssistant,
                                DictAssistant dictAssistant,
                                MapAssistant mapAssistant,
                                AssistantInfoService assistantInfoService,
                                ChatSessionService chatSessionService,
                                ChatMemoryStore chatMemoryStore) {
        this.defaultAssistant = defaultAssistant;
        this.dictAssistant = dictAssistant;
        this.mapAssistant = mapAssistant;
        this.assistantInfoService = assistantInfoService;
        this.chatSessionService = chatSessionService;
        this.chatMemoryStore = chatMemoryStore;
    }

    public Flux<String> streamingAssistant(String userId,
                                           Long sessionId,
                                           String message,
                                           String assistantCode,
                                           String legacyIntent) {
        Long finalSessionId = getOrCreateSessionId(userId, sessionId);
        AssistantInfoEntity assistant = resolveAssistant(assistantCode, legacyIntent);
        String memoryId = buildMemoryId(finalSessionId, assistant.getAssistantCode());
        String systemPrompt = AssistantPromptTemplates.resolveEffectivePrompt(
                assistant.getAssistantCode(),
                assistant.getAssistantKind(),
                assistant.getSystemPrompt()
        );

        String assistantKind = normalizeUpper(assistant.getAssistantKind());
        if (AssistantInfoService.ASSISTANT_KIND_DICT.equals(assistantKind)) {
            return dictAssistant.chat(memoryId, systemPrompt, message);
        }
        if (AssistantInfoService.ASSISTANT_KIND_MAP.equals(assistantKind)
                || AssistantPromptTemplates.MAP_ASSISTANT_CODE.equals(normalizeLower(assistant.getAssistantCode()))) {
            return mapAssistant.chat(memoryId, systemPrompt, message);
        }
        return defaultAssistant.chat(memoryId, systemPrompt, message);
    }

    public List<Map<String, Object>> getSessionMessages(Long sessionId, String assistantCode) {
        AssistantInfoEntity assistant = resolveAssistant(assistantCode, null);
        List<ChatMessage> messages = chatMemoryStore.getMessages(buildMemoryId(sessionId, assistant.getAssistantCode()));
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
        item.put("role", message.type().toString().toLowerCase(Locale.ROOT));

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

    private AssistantInfoEntity resolveAssistant(String assistantCode, String legacyIntent) {
        String candidate = firstNonBlank(assistantCode, legacyIntent);
        if (!StringUtils.hasText(candidate)) {
            AssistantInfoEntity defaultAssistant = assistantInfoService.getDefaultAssistant();
            if (defaultAssistant == null) {
                throw new ResponseStatusException(BAD_REQUEST, "请先维护并启用至少一个助手");
            }
            return defaultAssistant;
        }

        AssistantInfoEntity assistant = assistantInfoService.getEnabledByCode(candidate);
        if (assistant == null) {
            throw new ResponseStatusException(BAD_REQUEST, "助手不存在或未启用: " + candidate);
        }
        return assistant;
    }

    private String buildMemoryId(Long sessionId, String assistantCode) {
        return sessionId + MEMORY_ID_SEPARATOR + normalizeLower(assistantCode);
    }

    private String firstNonBlank(String primary, String fallback) {
        if (StringUtils.hasText(primary)) {
            return primary.trim();
        }
        if (StringUtils.hasText(fallback)) {
            return fallback.trim();
        }
        return null;
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

    private String normalizeLower(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeUpper(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}
