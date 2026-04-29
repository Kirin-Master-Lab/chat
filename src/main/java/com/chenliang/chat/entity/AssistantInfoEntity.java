package com.chenliang.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("assistant_info")
public class AssistantInfoEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String assistantCode;

    private String assistantName;

    private String assistantDesc;

    /**
     * 助手能力类型，当前支持 GENERAL、DICT。
     */
    private String assistantKind;

    private String systemPrompt;

    private Integer sortOrder;

    private Integer enabled;

    private Integer isDefault;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic(value = "0", delval = "id")
    private Integer deleted;
}
