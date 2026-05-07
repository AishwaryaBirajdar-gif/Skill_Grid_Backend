package com.SkillExchange.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    private String sender;    // The name
    private String senderId;  // ✅ Add this! It matches 'currentUserId' from React
    private String content;
    private String roomId;
    private String timestamp;
}