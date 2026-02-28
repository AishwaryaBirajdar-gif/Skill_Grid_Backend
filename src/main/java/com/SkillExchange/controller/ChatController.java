package com.SkillExchange.controller;

import com.SkillExchange.model.ChatMessage; 
import com.SkillExchange.service.RoomService; // ✅ Added Import
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

@Controller
@CrossOrigin("*")
public class ChatController {

    private final RoomService roomService; // ✅ Added Service Field

    // ✅ Added Constructor for Injection
    public ChatController(RoomService roomService) {
        this.roomService = roomService;
    }

    @MessageMapping("/sendMessage/{requestId}")
    @SendTo("/topic/room/{requestId}")
    public ChatMessage sendMessage(
            @DestinationVariable String requestId,
            ChatMessage message
    ) {
        // ✅ Save the message to MongoDB so history can be loaded later
        roomService.saveMessage(requestId, message); 
        
        // Broadcasts the message to everyone subscribed to /topic/room/{requestId}
        return message; 
    }
}