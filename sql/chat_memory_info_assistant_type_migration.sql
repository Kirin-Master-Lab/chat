ALTER TABLE chat_memory_info
    ADD COLUMN assistant_type VARCHAR(32) NOT NULL DEFAULT 'default' COMMENT '助手类型，例如 default、dict' AFTER session_id;

UPDATE chat_memory_info
SET assistant_type = 'dict'
WHERE session_id < 0;

UPDATE chat_memory_info
SET session_id = ABS(session_id)
WHERE session_id < 0;

ALTER TABLE chat_memory_info
    ADD UNIQUE INDEX uk_chat_memory_session_assistant (session_id, assistant_type);
