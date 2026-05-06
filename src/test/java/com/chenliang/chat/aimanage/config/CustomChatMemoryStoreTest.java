package com.chenliang.chat.aimanage.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomChatMemoryStoreTest {

    @Mock
    private ChatMemoryMapper chatMemoryMapper;

    @Mock
    private ChatSessionMapper chatSessionMapper;

    private CustomChatMemoryStore customChatMemoryStore;

    @BeforeEach
    void setUp() {
        customChatMemoryStore = new CustomChatMemoryStore(chatMemoryMapper, chatSessionMapper);
    }

    @Test
    void parseMemoryContextDefaultsAssistantTypeToDefault() throws Exception {
        Object context = invokeParseMemoryContext("12");

        assertNotNull(context);
        assertEquals(12L, invokeLongAccessor(context, "sessionId"));
        assertEquals("default", invokeStringAccessor(context, "assistantType"));
    }

    @Test
    void parseMemoryContextReadsExplicitAssistantType() throws Exception {
        Object context = invokeParseMemoryContext("15::dict");

        assertNotNull(context);
        assertEquals(15L, invokeLongAccessor(context, "sessionId"));
        assertEquals("dict", invokeStringAccessor(context, "assistantType"));
    }

    @Test
    void parseMemoryContextReturnsNullForInvalidMemoryId() throws Exception {
        Object context = invokeParseMemoryContext("abc::dict");

        assertNull(context);
    }

    @Test
    void updateMessagesInsertsAssistantTypeAndBaseSessionId() {
        when(chatMemoryMapper.selectOne(any())).thenReturn(null);

        ChatSessionEntity session = new ChatSessionEntity();
        session.setUserId("u-1");
        when(chatSessionMapper.selectById(15L)).thenReturn(session);

        customChatMemoryStore.updateMessages("15::dict", List.of());

        ArgumentCaptor<ChatMemoryEntity> captor = ArgumentCaptor.forClass(ChatMemoryEntity.class);
        verify(chatMemoryMapper).insert(captor.capture());

        ChatMemoryEntity entity = captor.getValue();
        assertEquals("u-1", entity.getUserId());
        assertEquals(15L, entity.getSessionId());
        assertEquals("dict", entity.getAssistantType());
        assertNotNull(entity.getMessageJson());
        assertNotNull(entity.getCreateTime());
        assertNotNull(entity.getUpdateTime());
    }

    @Test
    void updateMessagesSkipsInsertWhenSessionMissing() {
        when(chatMemoryMapper.selectOne(any())).thenReturn(null);
        when(chatSessionMapper.selectById(99L)).thenReturn(null);

        customChatMemoryStore.updateMessages("99::dict", List.of());

        verify(chatMemoryMapper, never()).insert(org.mockito.ArgumentMatchers.<ChatMemoryEntity>any());
    }

    @Test
    void getMessagesReturnsEmptyListWhenNoRecordExists() {
        when(chatMemoryMapper.selectOne(any())).thenReturn(null);

        List<?> messages = customChatMemoryStore.getMessages("15::dict");

        assertTrue(messages.isEmpty());
    }

    private Object invokeParseMemoryContext(String memoryId) throws Exception {
        Method method = CustomChatMemoryStore.class.getDeclaredMethod("parseMemoryContext", Object.class);
        method.setAccessible(true);
        return method.invoke(customChatMemoryStore, memoryId);
    }

    private long invokeLongAccessor(Object target, String methodName) throws Exception {
        Method accessor = target.getClass().getDeclaredMethod(methodName);
        accessor.setAccessible(true);
        return (Long) accessor.invoke(target);
    }

    private String invokeStringAccessor(Object target, String methodName) throws Exception {
        Method accessor = target.getClass().getDeclaredMethod(methodName);
        accessor.setAccessible(true);
        return (String) accessor.invoke(target);
    }
}
