package com.chenliang.chat.aiservice.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天记忆 Mapper 接口。
 * 基于 MyBatis-Plus 进行 CRUD 存储操作。
 */
@Mapper
public interface ChatMemoryMapper extends BaseMapper<ChatMemoryEntity> {
}
