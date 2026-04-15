package com.chenliang.chat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chenliang.chat.entity.ChatSessionEntity;
import com.chenliang.chat.mapper.ChatSessionMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


import java.time.LocalDateTime;
import java.util.List;

/**
 * 会话业务层
 */
@Service
public class ChatSessionService {

    @Resource
    private ChatSessionMapper chatSessionMapper;

    /**
     * 查询用户所有会话
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    public List<ChatSessionEntity> listByUserId(String userId) {
        LambdaQueryWrapper<ChatSessionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSessionEntity::getUserId, userId)
                .eq(ChatSessionEntity::getDeleted, 0)
                .orderByDesc(ChatSessionEntity::getUpdateTime);
        return chatSessionMapper.selectList(wrapper);
    }

    /**
     * 创建新会话
     *
     * @param userId      用户ID
     * @param sessionName 会话名称
     * @return 新会话
     */
    public ChatSessionEntity createSession(String userId, String sessionName) {


        LambdaQueryWrapper<ChatSessionEntity> query = Wrappers.lambdaQuery(ChatSessionEntity.class);
        query.eq(ChatSessionEntity::getUserId, userId);
        query.select(ChatSessionEntity::getId);
        List<ChatSessionEntity> existList = chatSessionMapper.selectList(query);
        long number;
        if (CollectionUtils.isEmpty(existList)) {
            number = 1;
        } else {
            number = existList.size() + 1;
        }
        ChatSessionEntity session = new ChatSessionEntity();
        session.setUserId(userId);
        session.setSessionName(sessionName + "-" + number);
        // 如果是用户第一个会话，设为默认
        long count = countByUserId(userId);
        session.setIsDefault(count == 0 ? 1 : 0);
        LocalDateTime now = LocalDateTime.now();
        session.setCreateTime(now);
        session.setUpdateTime(now);
        session.setDeleted(0);
        chatSessionMapper.insert(session);
        return session;
    }

    /**
     * 修改会话名称
     *
     * @param sessionId   会话ID
     * @param sessionName 新会话名称
     */
    public void updateSessionName(Long sessionId, String sessionName) {
        ChatSessionEntity session = new ChatSessionEntity();
        session.setId(sessionId);
        session.setSessionName(sessionName);
        session.setUpdateTime(LocalDateTime.now());
        chatSessionMapper.updateById(session);
    }

    /**
     * 删除会话
     *
     * @param sessionId 会话ID
     */
    public void deleteSession(Long sessionId) {
        chatSessionMapper.deleteById(sessionId);
    }

    /**
     * 获取用户默认会话
     *
     * @param userId 用户ID
     * @return 默认会话
     */
    public ChatSessionEntity getDefaultSession(String userId) {
        LambdaQueryWrapper<ChatSessionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSessionEntity::getUserId, userId)
                .eq(ChatSessionEntity::getIsDefault, 1)
                .eq(ChatSessionEntity::getDeleted, 0)
                .last("limit 1");
        return chatSessionMapper.selectOne(wrapper);
    }

    /**
     * 获取会话详情
     *
     * @param sessionId 会话ID
     * @return 会话信息
     */
    public ChatSessionEntity getById(Long sessionId) {
        return chatSessionMapper.selectById(sessionId);
    }

    /**
     * 统计用户会话数量
     *
     * @param userId 用户ID
     * @return 会话数量
     */
    private long countByUserId(String userId) {
        LambdaQueryWrapper<ChatSessionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSessionEntity::getUserId, userId)
                .eq(ChatSessionEntity::getDeleted, 0);
        return chatSessionMapper.selectCount(wrapper);
    }
}
