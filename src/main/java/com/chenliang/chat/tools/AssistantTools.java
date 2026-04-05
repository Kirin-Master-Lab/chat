package com.chenliang.chat.tools;

import dev.langchain4j.agent.tool.Tool;
import io.micrometer.observation.annotation.Observed;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

/**
 * AI 助手可调用的工具组件。
 * 通过在方法上添加 {@link Tool} 注解，LangChain4j 能够自动在聊天请求中包含该工具的描述。
 * 当 AI 认为需要调用此功能来生成回复时，会触发该方法的执行。
 */
@Component
public class AssistantTools {

    /**
     * 获取当前系统时间。
     * AI 在需要知道现在几点时会自动调用此工具。
     * 
     * @return 格式化后的当前时间字符串
     */
    @Tool
    @Observed
    public String currentTime() {
        return LocalTime.now().toString();
    }
}
