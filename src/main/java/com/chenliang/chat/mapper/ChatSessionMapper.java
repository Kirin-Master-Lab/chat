package com.chenliang.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chenliang.chat.entity.ChatSessionEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会话Mapper
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSessionEntity> {
}
