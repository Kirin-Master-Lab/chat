package com.chenliang.chat.controller;

import com.chenliang.chat.entity.ChatSessionEntity;
import com.chenliang.chat.service.ChatSessionService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

//import javax.annotation.Resource;
import java.util.List;

/**
 * 会话管理接口
 */
@RestController
@RequestMapping("/session")
public class ChatSessionController {

    @Resource
    private ChatSessionService chatSessionService;

    /**
     * 获取用户会话列表
     * @param userId 用户ID
     * @return 会话列表
     */
    @GetMapping("/list")
    public List<ChatSessionEntity> list(@RequestParam String userId) {
        return chatSessionService.listByUserId(userId);
    }

    /**
     * 创建新会话
     * @param userId 用户ID
     * @param sessionName 会话名称
     * @return 新会话
     */
    @PostMapping("/create")
    public ChatSessionEntity create(
            @RequestParam String userId,
            @RequestParam(defaultValue = "新会话") String sessionName) {
        return chatSessionService.createSession(userId, sessionName);
    }

    /**
     * 修改会话名称
     * @param sessionId 会话ID
     * @param sessionName 新会话名称
     */
    @PostMapping("/update")
    public void update(
            @RequestParam Long sessionId,
            @RequestParam String sessionName) {
        chatSessionService.updateSessionName(sessionId, sessionName);
    }

    /**
     * 删除会话
     * @param sessionId 会话ID
     */
    @PostMapping("/delete")
    public void delete(@RequestParam Long sessionId) {
        chatSessionService.deleteSession(sessionId);
    }
}
