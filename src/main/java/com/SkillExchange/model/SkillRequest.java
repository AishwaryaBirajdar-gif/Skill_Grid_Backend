package com.SkillExchange.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "requests")
public class SkillRequest {
    @Id
    private String id;
    
    private String senderId;    // Person asking
    private String receiverId;  // Person being asked
    
    private String skillRequested; // The skill User B has
    private String skillOffered;   // The skill User A will give in return
    
    private String status = "PENDING"; // PENDING, ACCEPTED, REJECTED
}