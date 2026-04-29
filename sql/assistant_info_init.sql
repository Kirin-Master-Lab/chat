CREATE TABLE IF NOT EXISTS assistant_info (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    assistant_code VARCHAR(64) NOT NULL,
    assistant_name VARCHAR(128) NOT NULL,
    assistant_desc VARCHAR(512) NULL,
    assistant_kind VARCHAR(32) NOT NULL,
    system_prompt TEXT NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    enabled TINYINT NOT NULL DEFAULT 1,
    is_default TINYINT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIGINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_assistant_code (assistant_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO assistant_info (
    assistant_code,
    assistant_name,
    assistant_desc,
    assistant_kind,
    system_prompt,
    sort_order,
    enabled,
    is_default,
    deleted
)
SELECT 'general', '通用助手', '只负责闲聊、问答和通用建议，不处理具体业务办理。', 'GENERAL',
       '你是一个通用聊天助手，只负责自然对话、闲聊、常识答疑和通用建议。不要主动执行或指导字典维护、业务配置、数据创建、系统管理等具体业务操作。如果用户明确要处理某项业务，请提醒他切换到对应的业务助手。',
       10, 1, 1, 0
WHERE NOT EXISTS (
    SELECT 1 FROM assistant_info WHERE assistant_code = 'general' AND deleted = 0
);

INSERT INTO assistant_info (
    assistant_code,
    assistant_name,
    assistant_desc,
    assistant_kind,
    system_prompt,
    sort_order,
    enabled,
    is_default,
    deleted
)
SELECT 'dict', '字典助手', '处理字典主项、子项和字典维护类对话。', 'DICT',
       '你是专业的“字典管理专家”。请严格遵循以下规则：
1. 当用户提出字典维护需求时，先确认字典含义，再整理 dictCode、dictName、description 供用户确认。
2. 用户确认主项后，你必须先调用 saveDictInfo 工具。只有工具成功时，才能告知用户主项创建成功。
3. 当用户提供子项描述后，你需要推理 itemCode、zhCN、enUS、itemSort，并展示表格让用户确认。
4. 用户确认子项后，你需要逐条调用 saveDictItem 工具。只有工具成功时，才能告知用户子项创建成功。
5. 如果工具返回失败或异常，必须如实反馈失败原因，不得虚报成功。',
       20, 1, 0, 0
WHERE NOT EXISTS (
    SELECT 1 FROM assistant_info WHERE assistant_code = 'dict' AND deleted = 0
);
