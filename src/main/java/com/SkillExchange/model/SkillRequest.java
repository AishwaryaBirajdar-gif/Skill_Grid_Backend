package com.SkillExchange.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.ArrayList;
import java.util.List;
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
    
    private List<String> senderRequirements = new ArrayList<>(); 
    private List<String> receiverRequirements = new ArrayList<>(); 

    private int senderProgress = 0; 
    private int receiverProgress = 0;

    private boolean senderLocked = false; 
    private boolean receiverLocked = false; 

    // Negotiation Flags
    private boolean senderRequirementsApproved = false; // Does Sender approve what Learner wrote?
    private boolean receiverRequirementsApproved = false; // Does Receiver approve what Learner wrote?
}