package com.chenliang.chat.lowlevel;

import dev.langchain4j.model.chat.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 低级别模型控制器。
 * 展示了如何直接使用 {@link ChatModel} 实例进行交互。
 * 相比于高级别的 AiService，直接操作 ChatModel 更加灵活，但也需要手动管理消息历史和上下文。
 */
@RestController
public class ChatModelController {

    private final ChatModel chatModel;

    /**
     * 构造函数注入底层聊天模型。
     * @param chatModel Spring Boot 自动配置的 ChatModel 实例
     */
    public ChatModelController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * 直接调用模型进行单次对话。
     * 该端点不会自动保留对话历史，每次请求都是独立的。
     * 
     * @param message 用户消息，默认为中文问候
     * @return 模型返回的文本结果
     */
    @GetMapping("/model")
    public String model(@RequestParam(value = "message", defaultValue = "现在几点了?") String message) {
        return chatModel.chat(message);
    }
}