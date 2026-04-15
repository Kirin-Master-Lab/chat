package com.chenliang.chat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话实体类
 */
@Data
@TableName("chat_session")
public class ChatSessionEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 会话名称
     */
    private String sessionName;

    /**
     * 是否是默认会话 1是 0否
     */
    private Integer isDefault;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记 1删除 0未删除
     */
    @TableLogic(value = "0", delval = "id")
    private Integer deleted;
}
