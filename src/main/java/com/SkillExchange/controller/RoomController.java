package com.SkillExchange.controller;

import com.SkillExchange.model.ChatMessage;
// Note: If you don't have a Room model, you might be storing 
// messages inside the SkillRequest or Exchange model instead.
import com.SkillExchange.service.RoomService; 
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms")
@CrossOrigin("http://localhost:5173")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<List<ChatMessage>> getMessages(@PathVariable String roomId) {
        // This method must exist in your RoomService to fetch history from DB
        List<ChatMessage> messages = roomService.getMessages(roomId);
        return ResponseEntity.ok(messages);
    }
}