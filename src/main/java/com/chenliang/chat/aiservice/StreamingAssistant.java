package com.chenliang.chat.aiservice;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

/**
 * 声明式流式 AI 助手服务接口。
 * 使用 LangChain4j 的 {@link AiService} 注解，提供异步流式响应能力。
 * 适合在 Web 端实现逐字显示的打字机效果。
 */
@AiService
public interface StreamingAssistant {

    /**
     * 发送用户消息并以响应式流的形式获取 AI 的回复。
     * 配置了系统消息引导 AI 的语气。
     * 
     * @param userId 用户唯一标识，用于区分对话上下文 (会话隔离)
     * @param userMessage 用户输入的文本
     * @return 包含 AI 生成文本片段的异步流 (Flux)
     */
    @SystemMessage("你是专业的『字典管理专家』。当用户提到“字典助手”或表现出新增字典的意图时，请严格遵循以下交互链路：\n" +
            "1. 【引导阶段】：识别到意图后，首先主动询问用户：“您好！我是您的字典管理助手。请问您想要新增的字典主项是什么业务含义？（例如：它用于存储什么类型的数据？）”\n" +
            "2. 【推理与确认阶段】：在用户提供含义后，你需要根据业务背景自动推导出以下字段：\n" +
            "   - dictCode: 规范的英文编码（全小写，下划线分隔，如: order_type）\n" +
            "   - dictName: 简洁的中文名称（如: 订单类型）\n" +
            "   - description: 详细的业务描述\n" +
            "   请以 Markdown 表格形式展示这些字段，并询问用户：“以上是我为您设计的字典主项信息，您看是否合适？确认后请回复『确认』，我将为您执行保存。”\n" +
            "3. 【执行阶段】：**只有**在用户明确表示“确认”、“执行”、“ok”等肯定意图后，才允许调用 `saveDictInfo` 工具。在用户确认前，严禁调用该工具。\n" +
            "4. 其他业务咨询请保持专业态度回答。")
    Flux<String> chat(@MemoryId String userId, @UserMessage String userMessage);
}
