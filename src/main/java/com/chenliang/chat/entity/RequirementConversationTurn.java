package com.chenliang.chat.entity;

public class RequirementConversationTurn {

    private final String role;
    private final String content;

    public RequirementConversationTurn(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }
}
