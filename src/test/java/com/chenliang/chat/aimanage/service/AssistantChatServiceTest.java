package com.chenliang.chat.aimanage.service;

import com.chenliang.chat.aimanage.aiservice.DefaultAssistant;
import com.chenliang.chat.aimanage.aiservice.DictAssistant;
import com.chenliang.chat.aimanage.prompt.PromptService;
import com.chenliang.chat.entity.ChatSessionEntity;
import com.chenliang.chat.service.ChatSessionService;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private PromptService promptService;

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
                promptService,
                chatSessionService,
                chatMemoryStore
        );
    }

    @Test
    void streamingAssistantRoutesToDictAssistant() {
        ChatSessionEntity session = new ChatSessionEntity();
        session.setId(11L);
        session.setUserId("u-1");

        when(chatSessionService.getById(11L)).thenReturn(session);
        when(promptService.getSystemPrompt(anyString())).thenReturn("dict-system");
        when(dictAssistant.chat(eq("11::dict"), eq("dict-system"), eq("hello")))
                .thenReturn(Flux.just("ok"));

        String result = assistantChatService
                .streamingAssistant("u-1", 11L, "hello", "dict", null)
                .collectList()
                .map(list -> String.join("", list))
                .block();

        assertEquals("ok", result);
        verify(dictAssistant).chat("11::dict", "dict-system", "hello");
        verify(defaultAssistant, never()).chat(anyString(), anyString(), anyString());
    }

    @Test
    void streamingAssistantFallsBackToDefaultAssistantWhenTypeUnknown() {
        ChatSessionEntity session = new ChatSessionEntity();
        session.setId(12L);
        session.setUserId("u-2");

        when(chatSessionService.getById(12L)).thenReturn(session);
        when(promptService.getSystemPrompt(anyString())).thenReturn("default-system");
        when(defaultAssistant.chat(eq("12::default"), eq("default-system"), eq("hello")))
                .thenReturn(Flux.just("done"));

        String result = assistantChatService
                .streamingAssistant("u-2", 12L, "hello", "other", null)
                .collectList()
                .map(list -> String.join("", list))
                .block();

        assertEquals("done", result);
        verify(defaultAssistant).chat("12::default", "default-system", "hello");
        verify(dictAssistant, never()).chat(anyString(), anyString(), anyString());
    }

    @Test
    void streamingAssistantCreatesNewSessionWhenSessionUnavailable() {
        ChatSessionEntity created = new ChatSessionEntity();
        created.setId(20L);

        when(chatSessionService.getById(anyLong())).thenReturn(null);
        when(chatSessionService.getDefaultSession("u-3")).thenReturn(null);
        when(chatSessionService.createSession("u-3", "新会话")).thenReturn(created);
        when(promptService.getSystemPrompt(anyString())).thenReturn("default-system");
        when(defaultAssistant.chat(eq("20::default"), eq("default-system"), eq("hello")))
                .thenReturn(Flux.just("created"));

        String result = assistantChatService
                .streamingAssistant("u-3", 999L, "hello", null, null)
                .collectList()
                .map(list -> String.join("", list))
                .block();

        assertEquals("created", result);
        verify(chatSessionService).createSession("u-3", "新会话");
        verify(defaultAssistant).chat("20::default", "default-system", "hello");
    }
}
