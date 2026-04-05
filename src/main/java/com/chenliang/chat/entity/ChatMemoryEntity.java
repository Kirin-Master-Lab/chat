package com.chenliang.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天记忆持久化实体类。
 * 用于映射数据库表 chat_memory_info，存储用户的历史对话。
 */
@Data
@TableName("chat_memory_info")
public class ChatMemoryEntity {

    /**
     * 主键 ID，自增。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户唯一标识，即 MemoryId。
     * 用于区分不同用户的对话。
     */
    private String userId;

    /**
     * 序列化后的消息 JSON 内容。
     * 存储 LangChain4j 的 ChatMessage 列表。
     * 配置maxMessages(10)是10, 这里的json数据的长度就是10
     */
    private String messageJson;

    /**
     * 记录创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 记录更新时间。
     */
    private LocalDateTime updateTime;
}
