package com.chenliang.chat.config;

import com.chenliang.chat.service.RequirementClarificationAiService;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RequirementAnalysisConfig {

    @Bean
    public ChatModel requirementAnalysisChatModel(
            @Value("${langchain4j.open-ai.chat-model.api-key:${ALI_API_KEY:}}") String apiKey,
            @Value("${langchain4j.open-ai.chat-model.base-url:https://api.univibe.cc/openai}") String baseUrl,
            @Value("${langchain4j.open-ai.chat-model.model-name:gpt-5.4}") String modelName
    ) {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .responseFormat("json_schema")
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    @Bean
    public RequirementClarificationAiService requirementClarificationAiService(ChatModel requirementAnalysisChatModel) {
        return AiServices.builder(RequirementClarificationAiService.class)
                .chatModel(requirementAnalysisChatModel)
                .build();
    }
}
