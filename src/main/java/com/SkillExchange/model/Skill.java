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
    
    private String type;        // "OFFERED" or "WANTED"
    private String userId;      // Owner ID
    private String karmaPoints;
    private String depthLevel;
    
    // ADDED: This field will now be recognized by Lombok
    private String status = "PENDING"; 
    
    private boolean isTrending = false;
}