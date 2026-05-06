package com.chenliang.chat.entity;

import java.util.List;

public class RequirementGuidance {

    private final String currentUnderstanding;
    private final List<String> missingInformation;
    private final List<String> nextQuestions;
    private final String assistantReply;

    public RequirementGuidance(String currentUnderstanding, List<String> missingInformation, List<String> nextQuestions, String assistantReply) {
        this.currentUnderstanding = currentUnderstanding;
        this.missingInformation = missingInformation;
        this.nextQuestions = nextQuestions;
        this.assistantReply = assistantReply;
    }

    public String getCurrentUnderstanding() {
        return currentUnderstanding;
    }

    public List<String> getMissingInformation() {
        return missingInformation;
    }

    public List<String> getNextQuestions() {
        return nextQuestions;
    }

    public String getAssistantReply() {
        return assistantReply;
    }
}
