package com.SkillExchange.service;

import com.SkillExchange.model.ChatMessage;
import com.SkillExchange.model.Room;
import com.SkillExchange.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    public List<ChatMessage> getMessages(String roomId) {
        Room room = roomRepository.findByRoomId(roomId);
        if (room != null) {
            return room.getMessages();
        }
        return new ArrayList<>(); // Return empty list if room doesn't exist yet
    }

    // You can call this from your ChatController to save new messages
    public void saveMessage(String roomId, ChatMessage message) {
        Room room = roomRepository.findByRoomId(roomId);
        if (room == null) {
            room = new Room();
            room.setRoomId(roomId);
        }
        room.getMessages().add(message);
        roomRepository.save(room);
    }
}