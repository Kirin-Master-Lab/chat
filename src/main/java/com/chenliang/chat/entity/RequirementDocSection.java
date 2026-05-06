package com.chenliang.chat.entity;

public class RequirementDocSection {

    private final String title;
    private final String content;

    public RequirementDocSection(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
