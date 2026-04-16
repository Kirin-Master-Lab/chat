package com.chenliang.chat.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chenliang.chat.entity.ChatMemoryEntity;
import com.chenliang.chat.entity.ChatSessionEntity;
import com.chenliang.chat.mapper.ChatMemoryMapper;
import com.chenliang.chat.mapper.ChatSessionMapper;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义聊天记忆存储。
 * 按助手类型拆分同一会话的上下文，避免通用助手和字典助手共用一份历史。
 */
@Component
public class CustomChatMemoryStore implements ChatMemoryStore {

    private static final String MEMORY_ID_SEPARATOR = "::";
    private static final String ASSISTANT_TYPE_DEFAULT = "default";
    private static final String ASSISTANT_TYPE_DICT = "dict";

    private final ChatMemoryMapper chatMemoryMapper;
    private final ChatSessionMapper chatSessionMapper;

    public CustomChatMemoryStore(ChatMemoryMapper chatMemoryMapper, ChatSessionMapper chatSessionMapper) {
        this.chatMemoryMapper = chatMemoryMapper;
        this.chatSessionMapper = chatSessionMapper;
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        MemoryContext memoryContext = parseMemoryContext(memoryId);
        if (memoryContext == null) {
            return new ArrayList<>();
        }

        ChatMemoryEntity entity = chatMemoryMapper.selectOne(
                new LambdaQueryWrapper<ChatMemoryEntity>()
                        .eq(ChatMemoryEntity::getSessionId, memoryContext.storageSessionId())
        );
        if (entity == null) {
            return new ArrayList<>();
        }
        return ChatMessageDeserializer.messagesFromJson(entity.getMessageJson());
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        MemoryContext memoryContext = parseMemoryContext(memoryId);
        if (memoryContext == null) {
            return;
        }

        String json = ChatMessageSerializer.messagesToJson(messages);
        LocalDateTime now = LocalDateTime.now();

        ChatMemoryEntity entity = chatMemoryMapper.selectOne(
                new LambdaQueryWrapper<ChatMemoryEntity>()
                        .eq(ChatMemoryEntity::getSessionId, memoryContext.storageSessionId())
        );

        if (entity == null) {
            ChatSessionEntity session = chatSessionMapper.selectById(memoryContext.baseSessionId());
            if (session == null) {
                return;
            }

            entity = new ChatMemoryEntity();
            entity.setUserId(session.getUserId());
            entity.setSessionId(memoryContext.storageSessionId());
            entity.setMessageJson(json);
            entity.setCreateTime(now);
            entity.setUpdateTime(now);
            chatMemoryMapper.insert(entity);
            return;
        }

        entity.setMessageJson(json);
        entity.setUpdateTime(now);
        chatMemoryMapper.updateById(entity);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        MemoryContext memoryContext = parseMemoryContext(memoryId);
        if (memoryContext == null) {
            return;
        }

        chatMemoryMapper.delete(
                new LambdaQueryWrapper<ChatMemoryEntity>()
                        .eq(ChatMemoryEntity::getSessionId, memoryContext.storageSessionId())
        );
    }

    private MemoryContext parseMemoryContext(Object memoryId) {
        String rawMemoryId = String.valueOf(memoryId);
        if (rawMemoryId == null || rawMemoryId.trim().isEmpty()) {
            return null;
        }

        String[] parts = rawMemoryId.split(MEMORY_ID_SEPARATOR, 2);
        Long baseSessionId;
        try {
            baseSessionId = Long.valueOf(parts[0].trim());
        } catch (NumberFormatException e) {
            return null;
        }

        String assistantType = parts.length > 1 ? parts[1].trim().toLowerCase() : ASSISTANT_TYPE_DEFAULT;
        Long storageSessionId = resolveStorageSessionId(baseSessionId, assistantType);
        return new MemoryContext(baseSessionId, storageSessionId);
    }

    private Long resolveStorageSessionId(Long baseSessionId, String assistantType) {
        if (ASSISTANT_TYPE_DICT.equals(assistantType)) {
            return -baseSessionId;
        }
        return baseSessionId;
    }

    private record MemoryContext(Long baseSessionId, Long storageSessionId) {
    }
}
