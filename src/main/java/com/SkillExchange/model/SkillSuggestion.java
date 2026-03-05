package com.SkillExchange.model;

import java.util.List;

public class SkillSuggestion {
    private String currentSkill;
    private String nextStep;
    private String explanation;
    private List<String> relatedSkills;

    // MANDATORY: Spring needs these Getters to fix the 406 error!
    public String getCurrentSkill() { return currentSkill; }
    public String getNextStep() { return nextStep; }
    public String getExplanation() { return explanation; }
    public List<String> getRelatedSkills() { return relatedSkills; }

    // Setters (already existing from previous steps)
    public void setCurrentSkill(String currentSkill) { this.currentSkill = currentSkill; }
    public void setNextStep(String nextStep) { this.nextStep = nextStep; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public void setRelatedSkills(List<String> relatedSkills) { this.relatedSkills = relatedSkills; }
}