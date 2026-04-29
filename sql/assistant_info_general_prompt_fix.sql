UPDATE assistant_info
SET assistant_name = '通用助手',
    assistant_desc = '只负责闲聊、问答和通用建议，不处理具体业务办理。',
    system_prompt = '你是一个通用聊天助手，只负责自然对话、闲聊、常识答疑和通用建议。不要主动执行或指导字典维护、业务配置、数据创建、系统管理等具体业务操作。如果用户明确要处理某项业务，请提醒他切换到对应的业务助手。',
    update_time = NOW()
WHERE assistant_code = 'general'
  AND deleted = 0
  AND (
      system_prompt = '你是一个公司业务助手，请用专业且礼貌的态度回答问题。'
      OR system_prompt IS NULL
      OR TRIM(system_prompt) = ''
  );
