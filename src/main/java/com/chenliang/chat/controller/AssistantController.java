package com.chenliang.chat.controller;

import com.chenliang.chat.aimanage.service.AssistantChatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

@RestController
public class AssistantController {

    private final AssistantChatService assistantChatService;

    public AssistantController(AssistantChatService assistantChatService) {
        this.assistantChatService = assistantChatService;
    }

    @GetMapping(value = "/streamingAssistant", produces = TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamingAssistant(
            @RequestParam(value = "userId", defaultValue = "user123") String userId,
            @RequestParam(value = "sessionId", required = false) Long sessionId,
            @RequestParam(value = "message", defaultValue = "你好") String message,
            @RequestParam(value = "assistantType", required = false) String assistantType,
            @RequestParam(value = "intent", required = false) String legacyIntent) {
        return assistantChatService.streamingAssistant(userId, sessionId, message, assistantType, legacyIntent);
    }

    @GetMapping("/session/messages")
    public List<Map<String, Object>> getSessionMessages(
            @RequestParam Long sessionId,
            @RequestParam(value = "assistantType", defaultValue = AssistantChatService.ASSISTANT_TYPE_DEFAULT) String assistantType) {
        return assistantChatService.getSessionMessages(sessionId, assistantType);
    }
}
