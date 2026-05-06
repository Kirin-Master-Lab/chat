package com.chenliang.chat.entity;

public class RequirementSessionStartResponse {

    private final String sessionId;
    private final RequirementSessionDraft draft;

    public RequirementSessionStartResponse(String sessionId, RequirementSessionDraft draft) {
        this.sessionId = sessionId;
        this.draft = draft;
    }

    public String getSessionId() {
        return sessionId;
    }

    public RequirementSessionDraft getDraft() {
        return draft;
    }
}
