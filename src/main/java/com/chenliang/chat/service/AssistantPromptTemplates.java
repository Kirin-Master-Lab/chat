package com.chenliang.chat.service;

import org.springframework.util.StringUtils;

public final class AssistantPromptTemplates {

    public static final String GENERAL_ASSISTANT_CODE = "general";
    public static final String DICT_ASSISTANT_CODE = "dict";
    public static final String MAP_ASSISTANT_CODE = "map";

    public static final String LEGACY_GENERAL_BUSINESS_PROMPT =
            "你是一个公司业务助手，请用专业且礼貌的态度回答问题。";

    public static final String GENERAL_CHAT_PROMPT =
            "你是一个通用聊天助手，只负责自然对话、闲聊、常识答疑和通用建议。"
                    + "不要主动执行或指导字典维护、业务配置、数据创建、系统管理等具体业务操作。"
                    + "如果用户明确要处理某项业务，请提醒他切换到对应的业务助手。";

    public static final String DICT_PROMPT =
            "你是专业的“字典管理专家”。请严格遵循以下规则：\n"
                    + "1. 当用户提出字典维护需求时，先确认字典含义，再整理 dictCode、dictName、description 供用户确认。\n"
                    + "2. 用户确认主项后，你必须先调用 saveDictInfo 工具。只有工具成功时，才能告知用户主项创建成功。\n"
                    + "3. 当用户提供子项描述后，你需要推理 itemCode、zhCN、enUS、itemSort，并展示表格让用户确认。\n"
                    + "4. 用户确认子项后，你需要逐条调用 saveDictItem 工具。只有工具成功时，才能告知用户子项创建成功。\n"
                    + "5. 如果工具返回失败或异常，必须如实反馈失败原因，不得虚报成功。";

    public static final String MAP_PROMPT =
            "你是专业的“地图与出行助手”。请严格遵循以下规则：\n"
                    + "1. 当用户要查地点、地址、经纬度、路线、周边信息或地图链接时，优先调用百度地图工具，不要凭空编造位置数据。\n"
                    + "2. 需要路线规划时，先确认起点、终点和出行方式；如果缺少百度坐标，先调用地点检索或地理编码工具补齐坐标。\n"
                    + "3. 当返回多个地点结果时，先简明列出候选项，必要时请用户确认后再继续路线或地图生成。\n"
                    + "4. 当用户要求“给我地图”或“发个地图链接”时，调用静态地图工具返回可直接打开的百度地图图片链接。\n"
                    + "5. 如果百度地图 AK 未配置、接口失败或结果为空，必须明确告诉用户当前失败原因，不得伪造地图结果。";

    private AssistantPromptTemplates() {
    }

    public static String resolveEffectivePrompt(String assistantCode, String assistantKind, String configuredPrompt) {
        String code = normalizeLower(assistantCode);
        String kind = normalizeUpper(assistantKind);
        String prompt = configuredPrompt == null ? "" : configuredPrompt.trim();

        if (DICT_ASSISTANT_CODE.equals(code) || AssistantInfoService.ASSISTANT_KIND_DICT.equals(kind)) {
            return StringUtils.hasText(prompt) ? prompt : DICT_PROMPT;
        }

        if (MAP_ASSISTANT_CODE.equals(code) || AssistantInfoService.ASSISTANT_KIND_MAP.equals(kind)) {
            return StringUtils.hasText(prompt) ? prompt : MAP_PROMPT;
        }

        if (GENERAL_ASSISTANT_CODE.equals(code)) {
            if (!StringUtils.hasText(prompt) || isLegacyGeneralBusinessPrompt(prompt)) {
                return GENERAL_CHAT_PROMPT;
            }
        }

        if (!StringUtils.hasText(prompt) && AssistantInfoService.ASSISTANT_KIND_GENERAL.equals(kind)) {
            return GENERAL_CHAT_PROMPT;
        }

        return prompt;
    }

    public static String resolveInitialPrompt(String assistantCode, String assistantKind, String configuredPrompt) {
        String prompt = configuredPrompt == null ? "" : configuredPrompt.trim();
        if (StringUtils.hasText(prompt)) {
            return prompt;
        }

        String code = normalizeLower(assistantCode);
        String kind = normalizeUpper(assistantKind);
        if (DICT_ASSISTANT_CODE.equals(code) || AssistantInfoService.ASSISTANT_KIND_DICT.equals(kind)) {
            return DICT_PROMPT;
        }
        if (MAP_ASSISTANT_CODE.equals(code) || AssistantInfoService.ASSISTANT_KIND_MAP.equals(kind)) {
            return MAP_PROMPT;
        }
        if (GENERAL_ASSISTANT_CODE.equals(code) || AssistantInfoService.ASSISTANT_KIND_GENERAL.equals(kind)) {
            return GENERAL_CHAT_PROMPT;
        }
        return prompt;
    }

    public static boolean isLegacyGeneralBusinessPrompt(String prompt) {
        return LEGACY_GENERAL_BUSINESS_PROMPT.equals(prompt == null ? null : prompt.trim());
    }

    private static String normalizeLower(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private static String normalizeUpper(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }
}
