package com.chenliang.chat.controller;

import com.chenliang.chat.entity.ChatSessionEntity;
import com.chenliang.chat.service.ChatSessionService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/session")
public class ChatSessionController {

    @Resource
    private ChatSessionService chatSessionService;

    @GetMapping("/list")
    public List<ChatSessionEntity> list(@RequestParam String userId) {
        return chatSessionService.listByUserId(userId);
    }

    @PostMapping("/create")
    public ChatSessionEntity create(
            @RequestParam String userId,
            @RequestParam(defaultValue = "新会话") String sessionName) {
        return chatSessionService.createSession(userId, sessionName);
    }

    @PostMapping("/update")
    public void update(
            @RequestParam Long sessionId,
            @RequestParam String sessionName) {
        chatSessionService.updateSessionName(sessionId, sessionName);
    }

    @PostMapping("/delete")
    public void delete(@RequestParam Long sessionId) {
        chatSessionService.deleteSession(sessionId);
    }
}
