package com.chenliang.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天记忆持久化实体，对应表 chat_memory_info。
 */
@Data
@TableName("chat_memory_info")
public class ChatMemoryEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 记录所属用户。
     */
    private String userId;

    /**
     * 基础会话 ID。
     */
    private Long sessionId;

    /**
     * 助手类型，例如 default、dict。
     */
    private String assistantType;

    /**
     * 序列化后的消息 JSON。
     */
    private String messageJson;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
