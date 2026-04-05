package com.chenliang.chat.aiservice;

import com.chenliang.chat.aiservice.dict.PromptRegistry;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

/**
 * AI 助手控制器。
 * 展示了如何使用高层 AI 服务（AiService）进行交互。
 */
@RestController
public class AssistantController {

    private static final String SESSION_MODE_KEY = "ASSISTANT_MODE";
    private static final String MODE_DICT = "DICT_EXPERT";

    private final Assistant assistant;
    private final StreamingAssistant streamingAssistant;

    public AssistantController(Assistant assistant, StreamingAssistant streamingAssistant) {
        this.assistant = assistant;
        this.streamingAssistant = streamingAssistant;
    }

    /**
     * 同步聊天端点。
     */
    @GetMapping("/assistant")
    public String assistant(
            HttpSession session,
            @RequestParam(value = "userId", defaultValue = "user123") String userId,
            @RequestParam(value = "message", defaultValue = "你好") String message) {
        
        String systemMessage = resolveSystemMessage(session, message);
        return assistant.chat(userId, systemMessage, message);
    }

    /**
     * 流式聊天端点。
     */
    @GetMapping(value = "/streamingAssistant", produces = TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamingAssistant(
            HttpSession session,
            @RequestParam(value = "userId", defaultValue = "user123") String userId,
            @RequestParam(value = "message", defaultValue = "你好") String message) {
        
        String systemMessage = resolveSystemMessage(session, message);
        return streamingAssistant.chat(userId, systemMessage, message);
    }

    /**
     * 实现带状态的意图分发。
     * 1. 优先根据关键词识别并【更新/进入】特定模式。
     * 2. 检查会话状态，如果处于特定模式，则持续输出该模式的 Prompt。
     * 3. 识别退出词并【重置】模式。
     */
    private String resolveSystemMessage(HttpSession session, String message) {
        // A. 意图识别：关键词触发进入字典模式
        if (message.contains("字典助手") || message.contains("新增字典") || message.contains("字典主项")) {
            session.setAttribute(SESSION_MODE_KEY, MODE_DICT);
            return PromptRegistry.DICTIONARY_EXPERT;
        }

        // B. 退出指令：重置回普通模式
        if (message.contains("退出模式") || message.contains("完成") || message.equals("取消") || message.contains("再见")) {
            session.removeAttribute(SESSION_MODE_KEY);
            return PromptRegistry.DEFAULT_ASSISTANT;
        }

        // C. 状态检查：如果当前 Session 锁定在字典模式，则无视关键词，继续加载字典专家角色
        Object mode = session.getAttribute(SESSION_MODE_KEY);
        if (MODE_DICT.equals(mode)) {
            return PromptRegistry.DICTIONARY_EXPERT;
        }

        // D. 默认返回普通助手
        return PromptRegistry.DEFAULT_ASSISTANT;
    }
}