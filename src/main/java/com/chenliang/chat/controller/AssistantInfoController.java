package com.chenliang.chat.controller;

import com.chenliang.chat.entity.AssistantInfoEntity;
import com.chenliang.chat.service.AssistantInfoService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/assistants")
public class AssistantInfoController {

    private final AssistantInfoService assistantInfoService;

    public AssistantInfoController(AssistantInfoService assistantInfoService) {
        this.assistantInfoService = assistantInfoService;
    }

    @GetMapping("/enabled")
    public List<Map<String, Object>> listEnabled() {
        return assistantInfoService.listEnabled()
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @GetMapping("/manage/list")
    public List<Map<String, Object>> listAll() {
        return assistantInfoService.listAll()
                .stream()
                .map(this::toDetail)
                .toList();
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody AssistantInfoEntity assistantInfo) {
        return toDetail(assistantInfoService.create(assistantInfo));
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody AssistantInfoEntity assistantInfo) {
        assistantInfo.setId(id);
        return toDetail(assistantInfoService.update(assistantInfo));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        assistantInfoService.delete(id);
    }

    private Map<String, Object> toSummary(AssistantInfoEntity assistant) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", assistant.getId());
        result.put("assistantCode", assistant.getAssistantCode());
        result.put("assistantName", assistant.getAssistantName());
        result.put("assistantDesc", assistant.getAssistantDesc());
        result.put("assistantKind", assistant.getAssistantKind());
        result.put("sortOrder", assistant.getSortOrder());
        result.put("enabled", assistant.getEnabled());
        result.put("isDefault", assistant.getIsDefault());
        return result;
    }

    private Map<String, Object> toDetail(AssistantInfoEntity assistant) {
        Map<String, Object> result = toSummary(assistant);
        result.put("systemPrompt", assistant.getSystemPrompt());
        result.put("createTime", assistant.getCreateTime());
        result.put("updateTime", assistant.getUpdateTime());
        return result;
    }
}
