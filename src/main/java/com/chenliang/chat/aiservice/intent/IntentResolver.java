package com.chenliang.chat.aiservice.intent;

/**
 * 意图识别接口。
 * 用于根据用户输入和当前上下文识别用户的业务意图。
 */
public interface IntentResolver {

    /**
     * 设置模式状态的关键常量
     */
    String MODE_DICT = "DICT_EXPERT";
    String MODE_DEFAULT = "DEFAULT";

    /**
     * 识别用户意图。
     *
     * @param userId      用户ID
     * @param message     用户最后一条消息
     * @param currentMode 当前持有的模式信息（如果已在某种模式中）
     * @return 识别到的意图/模式代码
     */
    String resolveIntent(String userId, String message, String currentMode);
}
