package com.SkillExchange.controller;

import com.SkillExchange.model.ChatMessage;
import com.SkillExchange.service.RoomService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.LocalDateTime;

@Controller
@CrossOrigin("*")
public class ChatController {

    private final RoomService roomService;

    public ChatController(RoomService roomService) {
        this.roomService = roomService;
    }

    @MessageMapping("/sendMessage/{requestId}")
    @SendTo("/topic/room/{requestId}")
    public ChatMessage sendMessage(
            @DestinationVariable String requestId,
            ChatMessage message
    ) {
        // ✅ 1. Set timestamp for history and sorting
        try {
            message.setTimestamp(LocalDateTime.now().toString());
        } catch (Exception e) {
            System.out.println("Timestamp field missing in ChatMessage model");
        }

        // ✅ 2. Save the message (including senderId) to MongoDB
        // This ensures the history persists after a reload
        roomService.saveMessage(requestId, message); 
        
        // ✅ 3. Broadcast the message
        // The message object now contains senderId, so the frontend can align it
        return message; 
    }
}