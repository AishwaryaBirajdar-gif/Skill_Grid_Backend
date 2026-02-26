package com.SkillExchange.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "requests")
public class SkillRequest {
    @Id
    private String id;
    
    private String senderId;
    private String receiverId;
    
    private String skillRequested;
    private String skillOffered;
    
    private String status = "PENDING";
    
    private String senderName;
    private String receiverName;
}