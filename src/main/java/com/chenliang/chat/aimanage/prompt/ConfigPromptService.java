package com.chenliang.chat.aimanage.prompt;

import com.chenliang.chat.aimanage.dict.PromptRegistry;
import org.springframework.stereotype.Service;

/**
 * 基于配置/静态仓库实现的提示词服务（未来可以扩展为从数据库获取插件）。
 */
@Service
public class ConfigPromptService implements PromptService {

    private static final String PROMPT_MODE_DICT = "DICT_EXPERT";

    @Override
    public String getSystemPrompt(String intentCode) {
        if (PROMPT_MODE_DICT.equals(intentCode)) {
            return PromptRegistry.DICTIONARY_EXPERT;
        }

        // 默认返回通用提示词
        return PromptRegistry.DEFAULT_ASSISTANT;
    }
}
