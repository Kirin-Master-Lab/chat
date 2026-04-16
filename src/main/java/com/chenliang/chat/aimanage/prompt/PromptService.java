package com.chenliang.chat.aimanage.prompt;

/**
 * 提示词管理服务。
 * 屏蔽 Prompt 获取的底层细节（内存、数据库、配置中心等）。
 */
public interface PromptService {

    /**
     * 获取对应意图编码的系统提示词。
     *
     * @param intentCode 意图编码 (如 DICT_EXPERT)
     * @return 完整提示词内容
     */
    String getSystemPrompt(String intentCode);
}
