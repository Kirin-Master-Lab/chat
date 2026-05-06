package com.chenliang.chat.controller;

import com.chenliang.chat.entity.RequirementConversationRequest;
import com.chenliang.chat.entity.RequirementSessionDraft;
import com.chenliang.chat.entity.RequirementSessionStartResponse;
import com.chenliang.chat.service.RequirementWorkshopService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/requirement-workshop")
public class RequirementWorkshopController {

    private final RequirementWorkshopService requirementWorkshopService;

    public RequirementWorkshopController(RequirementWorkshopService requirementWorkshopService) {
        this.requirementWorkshopService = requirementWorkshopService;
    }

    @PostMapping("/sessions")
    public RequirementSessionStartResponse startSession() {
        RequirementSessionDraft draft = requirementWorkshopService.startSession();
        return new RequirementSessionStartResponse(draft.getSessionId(), draft);
    }

    @PostMapping("/conversation")
    public RequirementSessionDraft continueConversation(@RequestBody RequirementConversationRequest request) {
        return requirementWorkshopService.continueConversation(request.getSessionId(), request.getMessage());
    }

    @PostMapping("/sessions/{sessionId}/document")
    public RequirementSessionDraft generateDocument(@PathVariable String sessionId) {
        return requirementWorkshopService.generateDocument(sessionId);
    }

    @GetMapping("/sessions/{sessionId}")
    public RequirementSessionDraft getSession(@PathVariable String sessionId) {
        return requirementWorkshopService.getSessionDraft(sessionId);
    }
}
