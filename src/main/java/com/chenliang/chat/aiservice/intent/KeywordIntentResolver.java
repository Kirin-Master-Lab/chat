package com.chenliang.chat.aiservice.intent;

import org.springframework.stereotype.Service;

/**
 * 基础关键词意图识别器。
 * 封装了原本在 Controller 层中的硬编码语义判断逻辑。
 */
@Service
public class KeywordIntentResolver implements IntentResolver {

    @Override
    public String resolveIntent(String userId, String message, String currentMode) {
        // A. 显式意图触发词 (进入/更新模式)
        if (message.contains("字典助手") || message.contains("新增字典") || message.contains("字典主项")) {
            return MODE_DICT;
        }

        // B. 显式退出词 (重置回默认模式)
        if (message.contains("退出模式") || message.contains("完成") || message.equals("取消") || message.contains("再见")) {
            return MODE_DEFAULT;
        }

        // C. 隐式延续模式：如果已经在某个模式中，则继续保持
        if (currentMode != null && !MODE_DEFAULT.equals(currentMode)) {
            return currentMode;
        }

        // D. 默认普通助手
        return MODE_DEFAULT;
    }
}
