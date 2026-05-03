package com.SkillExchange.DTO;

public class SkillResponse {

    private String skill;
    private String level;
    private String description;

    public SkillResponse() {}

    public SkillResponse(String skill, String level, String description) {
        this.skill = skill;
        this.level = level;
        this.description = description;
    }

    public String getSkill() { return skill; }
    public void setSkill(String skill) { this.skill = skill; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}