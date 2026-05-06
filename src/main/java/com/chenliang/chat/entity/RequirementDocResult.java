package com.chenliang.chat.entity;

import java.util.List;

public class RequirementDocResult {

    private final String title;
    private final String overview;
    private final List<RequirementDocSection> sections;
    private final List<String> openQuestions;

    public RequirementDocResult(String title, String overview, List<RequirementDocSection> sections, List<String> openQuestions) {
        this.title = title;
        this.overview = overview;
        this.sections = sections;
        this.openQuestions = openQuestions;
    }

    public String getTitle() {
        return title;
    }

    public String getOverview() {
        return overview;
    }

    public List<RequirementDocSection> getSections() {
        return sections;
    }

    public List<String> getOpenQuestions() {
        return openQuestions;
    }
}
