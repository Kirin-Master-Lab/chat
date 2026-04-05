package com.chenliang.chat.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chenliang.chat.entity.ChatMemoryEntity;
import com.chenliang.chat.mapper.ChatMemoryMapper;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义聊天记忆存储。
 * 实现 {@link ChatMemoryStore} 接口，将 LangChain4j 的对话消息持久化到 MySQL 数据库中。
 * 支持基于 userId (MemoryId) 的会话隔离和持久化。
 */
@Component
public class CustomChatMemoryStore implements ChatMemoryStore {

    private final ChatMemoryMapper chatMemoryMapper;

    /**
     * 构造函数注入 Mapper。
     * @param chatMemoryMapper 聊天记忆 Mapper
     */
    public CustomChatMemoryStore(ChatMemoryMapper chatMemoryMapper) {
        this.chatMemoryMapper = chatMemoryMapper;
    }

    /**
     * 根据 MemoryId (在此为 userId) 获取历史消息。
     * @param memoryId 用户标识
     * @return 历史消息列表
     */
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String userId = String.valueOf(memoryId);
        ChatMemoryEntity entity = chatMemoryMapper.selectOne(
                new LambdaQueryWrapper<ChatMemoryEntity>().eq(ChatMemoryEntity::getUserId, userId)
        );
        if (entity == null) {
            return new ArrayList<>();
        }
        // 使用 LangChain4j 提供的反序列化工具将 JSON 转为消息对象列表
        return ChatMessageDeserializer.messagesFromJson(entity.getMessageJson());
    }

    /**
     * 更新 MemoryId 对应的消息。
     * 每次对话发生变化时（如用户提问或 AI 回答后），LangChain4j 会自动调用此方法。
     * @param memoryId 用户标识
     * @param messages 最新的完整消息列表
     */
    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String userId = String.valueOf(memoryId);
        // 使用 LangChain4j 提供的序列化工具将消息对象列表转为 JSON
        String json = ChatMessageSerializer.messagesToJson(messages);

        ChatMemoryEntity entity = chatMemoryMapper.selectOne(
                new LambdaQueryWrapper<ChatMemoryEntity>().eq(ChatMemoryEntity::getUserId, userId)
        );

        if (entity == null) {
            // 新用户开启对话，插入新记录
            entity = new ChatMemoryEntity();
            entity.setUserId(userId);
            entity.setMessageJson(json);
            chatMemoryMapper.insert(entity);
        } else {
            // 已有用户，更新消息内容
            entity.setMessageJson(json);
            chatMemoryMapper.updateById(entity);
        }
    }

    /**
     * 删除指定 MemoryId 的所有消息。
     * @param memoryId 用户标识
     */
    @Override
    public void deleteMessages(Object memoryId) {
        String userId = String.valueOf(memoryId);
        chatMemoryMapper.delete(
                new LambdaQueryWrapper<ChatMemoryEntity>().eq(ChatMemoryEntity::getUserId, userId)
        );
    }
}
