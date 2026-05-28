package com.SkillExchange.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;       // ✅ Added Import
import java.util.HashMap;   // ✅ Added Import

@Data 
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "requests")
public class SkillRequest {
    @Id
    private String id;
    private String senderId;
    private String receiverId;
    private String senderName; 
    private String receiverName;
    private String skillRequested; 
    private String skillOffered;   
    private String status; 
    
    private List<Milestone> milestones = new ArrayList<>(); 
    private List<String> senderRequirements = new ArrayList<>(); 
    private List<String> receiverRequirements = new ArrayList<>(); 
    
    private boolean senderReqNeedsUpdate = false;
    private boolean receiverReqNeedsUpdate = false;

    private int totalKarmaPrice = 50; 
    private int senderProgress = 0; 
    private int receiverProgress = 0; 
    private boolean senderLocked = false; 
    private boolean receiverLocked = false; 
    private boolean midwayKarmaTransferred = false; 

    private boolean senderRequirementsApproved = false;
    private boolean receiverRequirementsApproved = false;
    
    private List<ChatMessage> messages = new ArrayList<>();

    // ✅ NEW FIELD: Tracks if sender/receiver have given feedback
    // This allows the "Feedback Modal" to know when to hide.
    private Map<String, Boolean> feedbackStatus = new HashMap<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Milestone {
        private String id;
        private String title;
        private String description;
        private String status; 
        private boolean senderConfirmed;
        private boolean receiverConfirmed;
        private int karmaValue;
    }
}