package com.chenliang.chat.entity;

import java.util.List;

public class RequirementStructuredAnalysis {

    private String title;
    private String currentUnderstanding;
    private List<String> functionalScope;
    private List<String> businessFlow;
    private List<String> dataPoints;
    private List<String> nonFunctionalRequirements;
    private List<String> risks;
    private List<String> missingInformation;
    private List<String> nextQuestions;

    public RequirementStructuredAnalysis() {
    }

    public RequirementStructuredAnalysis(
            String title,
            String currentUnderstanding,
            List<String> functionalScope,
            List<String> businessFlow,
            List<String> dataPoints,
            List<String> nonFunctionalRequirements,
            List<String> risks,
            List<String> missingInformation,
            List<String> nextQuestions
    ) {
        this.title = title;
        this.currentUnderstanding = currentUnderstanding;
        this.functionalScope = functionalScope;
        this.businessFlow = businessFlow;
        this.dataPoints = dataPoints;
        this.nonFunctionalRequirements = nonFunctionalRequirements;
        this.risks = risks;
        this.missingInformation = missingInformation;
        this.nextQuestions = nextQuestions;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCurrentUnderstanding() {
        return currentUnderstanding;
    }

    public void setCurrentUnderstanding(String currentUnderstanding) {
        this.currentUnderstanding = currentUnderstanding;
    }

    public List<String> getFunctionalScope() {
        return functionalScope;
    }

    public void setFunctionalScope(List<String> functionalScope) {
        this.functionalScope = functionalScope;
    }

    public List<String> getBusinessFlow() {
        return businessFlow;
    }

    public void setBusinessFlow(List<String> businessFlow) {
        this.businessFlow = businessFlow;
    }

    public List<String> getDataPoints() {
        return dataPoints;
    }

    public void setDataPoints(List<String> dataPoints) {
        this.dataPoints = dataPoints;
    }

    public List<String> getNonFunctionalRequirements() {
        return nonFunctionalRequirements;
    }

    public void setNonFunctionalRequirements(List<String> nonFunctionalRequirements) {
        this.nonFunctionalRequirements = nonFunctionalRequirements;
    }

    public List<String> getRisks() {
        return risks;
    }

    public void setRisks(List<String> risks) {
        this.risks = risks;
    }

    public List<String> getMissingInformation() {
        return missingInformation;
    }

    public void setMissingInformation(List<String> missingInformation) {
        this.missingInformation = missingInformation;
    }

    public List<String> getNextQuestions() {
        return nextQuestions;
    }

    public void setNextQuestions(List<String> nextQuestions) {
        this.nextQuestions = nextQuestions;
    }
}
