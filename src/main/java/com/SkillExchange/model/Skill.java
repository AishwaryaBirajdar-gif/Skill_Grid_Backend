package com.SkillExchange.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "skills")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Skill {

    @Id
    private String id;

    private String skillName;
    private String category;
    private String description;
    
    // ADD THESE TWO FIELDS
    private String type;      // Will store "OFFERED" or "WANTED"
    private String userId;    // To know which user this skill belongs to
    
    private boolean isTrending = false;
}