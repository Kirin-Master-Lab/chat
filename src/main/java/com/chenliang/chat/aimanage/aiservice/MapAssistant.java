package com.chenliang.chat.aimanage.aiservice;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

@AiService(tools = "baiduMapTools")
public interface MapAssistant {

    @SystemMessage("{{systemMessage}}")
    Flux<String> chat(@MemoryId String memoryId,
                      @dev.langchain4j.service.V("systemMessage") String systemMessage,
                      @UserMessage String userMessage);
}
