package com.chenliang.chat.listener;


import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自定义聊天模型监听器。
 * 实现了 {@link ChatModelListener} 接口，用于监控地层大语言模型的交互过程。
 * 这是实现 AI 应用可观测性（Observability）的重要组件。
 */
public class MyChatModelListener implements ChatModelListener {

    private static final Logger log = LoggerFactory.getLogger(MyChatModelListener.class);

    /**
     * 在向大语言模型发送请求之前触发。
     * 可以用来记录提示词（Prompt）、模型参数等原始请求信息。
     * 
     * @param requestContext 包含请求详情的内容上下文
     */
    @Override
    public void onRequest(ChatModelRequestContext requestContext) {
        log.info("模型请求开始 (onRequest): {}", requestContext.chatRequest());
    }

    /**
     * 在模型成功返回响应后触发。
     * 可以用来记录模型生成的文本、Token 消耗情况（Usage）以及调用时长。
     * 
     * @param responseContext 包含响应结果的内容上下文
     */
    @Override
    public void onResponse(ChatModelResponseContext responseContext) {
        log.info("模型响应完成 (onResponse): {}", responseContext.chatResponse());
    }

    /**
     * 在模型调用过程中发生错误（如网络问题、Token 超限或模型报错）时触发。
     * 
     * @param errorContext 包含异常信息的内容上下文
     */
    @Override
    public void onError(ChatModelErrorContext errorContext) {
        log.error("模型调用异常 (onError): {}", errorContext.error().getMessage());
    }
}
