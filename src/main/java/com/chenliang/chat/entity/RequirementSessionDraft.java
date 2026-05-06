package com.chenliang.chat.entity;

import java.util.List;

public class RequirementSessionDraft {

    private final String sessionId;
    private final String stage;
    private final List<RequirementConversationTurn> conversation;
    private final RequirementGuidance guidance;
    private final RequirementDocResult document;

    public RequirementSessionDraft(String sessionId, String stage, List<RequirementConversationTurn> conversation, RequirementGuidance guidance, RequirementDocResult document) {
        this.sessionId = sessionId;
        this.stage = stage;
        this.conversation = conversation;
        this.guidance = guidance;
        this.document = document;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getStage() {
        return stage;
    }

    public List<RequirementConversationTurn> getConversation() {
        return conversation;
    }

    public RequirementGuidance getGuidance() {
        return guidance;
    }

    public RequirementDocResult getDocument() {
        return document;
    }
}
