package com.SkillExchange.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;
import lombok.Data; 

@Data
@Document(collection = "credentials")
public class Credential {
    @Id
    private String id;
    private String userId;
    private String requestId;
    private String skillName;
    private String partnerName;
    private double finalRating;
    private String feedbackComment;
    private int milestonesCompleted;
    private String issueDate;
    private String uniqueSerial; // e.g., SG-2026-A1B2
    
    // Getters and Setters
}