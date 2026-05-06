package com.chenliang.chat.aimanage.service;

import com.chenliang.chat.aimanage.aiservice.DefaultAssistant;
import com.chenliang.chat.aimanage.aiservice.DictAssistant;
import com.chenliang.chat.aimanage.aiservice.MapAssistant;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssistantChatServiceTest {

    @Mock
    private DefaultAssistant defaultAssistant;

    @Mock
    private DictAssistant dictAssistant;

    @Mock
    private MapAssistant mapAssistant;

    @Mock
    private AssistantInfoService assistantInfoService;

    @Mock
    private ChatSessionService chatSessionService;

    @Mock
    private ChatMemoryStore chatMemoryStore;

    private AssistantChatService assistantChatService;

    @BeforeEach
    void setUp() {
        assistantChatService = new AssistantChatService(
                defaultAssistant,
                dictAssistant,
                mapAssistant,
                assistantInfoService,
                chatSessionService,
                chatMemoryStore
        );
    }

    @Test
    void streamingAssistantRoutesToDictAssistant() {
        ChatSessionEntity session = buildSession(11L, "u-1");
        AssistantInfoEntity assistant = buildAssistant("dict", AssistantInfoService.ASSISTANT_KIND_DICT, "dict-system");

        when(chatSessionService.getById(11L)).thenReturn(session);
        when(assistantInfoService.getEnabledByCode("dict")).thenReturn(assistant);
        when(dictAssistant.chat("11::dict", "dict-system", "hello")).thenReturn(Flux.just("ok"));

        String result = assistantChatService
                .streamingAssistant("u-1", 11L, "hello", "dict", null)
                .collectList()
                .map(list -> String.join("", list))
                .block();

        assertEquals("ok", result);
        verify(dictAssistant).chat("11::dict", "dict-system", "hello");
        verify(defaultAssistant, never()).chat(anyString(), anyString(), anyString());
        verify(mapAssistant, never()).chat(anyString(), anyString(), anyString());
    }

    @Test
    void streamingAssistantRoutesToMapAssistant() {
        ChatSessionEntity session = buildSession(21L, "u-map");
        AssistantInfoEntity assistant = buildAssistant("map", AssistantInfoService.ASSISTANT_KIND_MAP, "");

        when(chatSessionService.getById(21L)).thenReturn(session);
        when(assistantInfoService.getEnabledByCode("map")).thenReturn(assistant);
        when(mapAssistant.chat("21::map", AssistantPromptTemplates.MAP_PROMPT, "查北京天安门"))
                .thenReturn(Flux.just("地图结果"));

        String result = assistantChatService
                .streamingAssistant("u-map", 21L, "查北京天安门", "map", null)
                .collectList()
                .map(list -> String.join("", list))
                .block();

        assertEquals("地图结果", result);
        verify(mapAssistant).chat("21::map", AssistantPromptTemplates.MAP_PROMPT, "查北京天安门");
        verify(defaultAssistant, never()).chat(anyString(), anyString(), anyString());
        verify(dictAssistant, never()).chat(anyString(), anyString(), anyString());
    }

    @Test
    void streamingAssistantUsesDefaultAssistantWhenCodeMissing() {
        ChatSessionEntity session = buildSession(12L, "u-2");
        AssistantInfoEntity assistant = buildAssistant("general", AssistantInfoService.ASSISTANT_KIND_GENERAL, "default-system");

        when(chatSessionService.getById(12L)).thenReturn(session);
        when(assistantInfoService.getDefaultAssistant()).thenReturn(assistant);
        when(defaultAssistant.chat("12::general", "default-system", "hello")).thenReturn(Flux.just("done"));

        String result = assistantChatService
                .streamingAssistant("u-2", 12L, "hello", null, null)
                .collectList()
                .map(list -> String.join("", list))
                .block();

        assertEquals("done", result);
        verify(defaultAssistant).chat("12::general", "default-system", "hello");
        verify(dictAssistant, never()).chat(anyString(), anyString(), anyString());
        verify(mapAssistant, never()).chat(anyString(), anyString(), anyString());
    }

    @Test
    void streamingAssistantReplacesLegacyBusinessPromptForGeneralAssistant() {
        ChatSessionEntity session = buildSession(14L, "u-legacy");
        AssistantInfoEntity assistant = buildAssistant(
                "general",
                AssistantInfoService.ASSISTANT_KIND_GENERAL,
                AssistantPromptTemplates.LEGACY_GENERAL_BUSINESS_PROMPT
        );

        when(chatSessionService.getById(14L)).thenReturn(session);
        when(assistantInfoService.getEnabledByCode("general")).thenReturn(assistant);
        when(defaultAssistant.chat("14::general", AssistantPromptTemplates.GENERAL_CHAT_PROMPT, "你是谁"))
                .thenReturn(Flux.just("我是通用聊天助手"));

        String result = assistantChatService
                .streamingAssistant("u-legacy", 14L, "你是谁", "general", null)
                .collectList()
                .map(list -> String.join("", list))
                .block();

        assertEquals("我是通用聊天助手", result);
        verify(defaultAssistant).chat("14::general", AssistantPromptTemplates.GENERAL_CHAT_PROMPT, "你是谁");
    }

    @Test
    void streamingAssistantCreatesNewSessionWhenSessionUnavailable() {
        ChatSessionEntity created = new ChatSessionEntity();
        created.setId(20L);
        AssistantInfoEntity assistant = buildAssistant("general", AssistantInfoService.ASSISTANT_KIND_GENERAL, "default-system");

        when(chatSessionService.getById(anyLong())).thenReturn(null);
        when(chatSessionService.getDefaultSession("u-3")).thenReturn(null);
        when(chatSessionService.createSession("u-3", "新会话")).thenReturn(created);
        when(assistantInfoService.getDefaultAssistant()).thenReturn(assistant);
        when(defaultAssistant.chat("20::general", "default-system", "hello")).thenReturn(Flux.just("created"));

        String result = assistantChatService
                .streamingAssistant("u-3", 999L, "hello", null, null)
                .collectList()
                .map(list -> String.join("", list))
                .block();

        assertEquals("created", result);
        verify(chatSessionService).createSession("u-3", "新会话");
        verify(defaultAssistant).chat("20::general", "default-system", "hello");
    }

    @Test
    void streamingAssistantThrowsWhenAssistantCodeUnknown() {
        ChatSessionEntity session = buildSession(13L, "u-4");

        when(chatSessionService.getById(13L)).thenReturn(session);
        when(assistantInfoService.getEnabledByCode("missing")).thenReturn(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> assistantChatService.streamingAssistant("u-4", 13L, "hello", "missing", null)
        );

        assertEquals("400 BAD_REQUEST \"助手不存在或未启用: missing\"", exception.getMessage());
    }

    private ChatSessionEntity buildSession(Long id, String userId) {
        ChatSessionEntity session = new ChatSessionEntity();
        session.setId(id);
        session.setUserId(userId);
        return session;
    }

    private AssistantInfoEntity buildAssistant(String assistantCode, String assistantKind, String systemPrompt) {
        AssistantInfoEntity assistant = new AssistantInfoEntity();
        assistant.setAssistantCode(assistantCode);
        assistant.setAssistantKind(assistantKind);
        assistant.setSystemPrompt(systemPrompt);
        return assistant;
    }
}
