package com.SkillExchange.service;

import com.SkillExchange.model.ChatMessage;
import com.SkillExchange.model.SkillRequest;
import com.SkillExchange.repository.SkillRepository;
import com.SkillExchange.repository.SkillRequestRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoomService {

    @Autowired
    private SkillRequestRepository requestRepository; 

    /**
     * Retrieves all saved messages for a specific barter request room.
     */
    public List<ChatMessage> getMessages(String requestId) {
        return requestRepository.findById(requestId)
                .map(SkillRequest::getMessages)
                .orElse(new ArrayList<>());
    }

    /**
     * Appends a new message to the SkillRequest document and persists it to MongoDB.
     */
    public void saveMessage(String requestId, ChatMessage message) {
        // 1. Retrieve the existing request document
        SkillRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Chat Room (Request) not found with ID: " + requestId));

        // 2. Ensure the messages list is initialized (Null safety)
        if (request.getMessages() == null) {
            request.setMessages(new ArrayList<>());
        }

        // 3. Add the new message to the list
        request.getMessages().add(message);

        // 4. Save the entire document back to MongoDB
        // This is the critical step for persistence!
        requestRepository.save(request);
    }
}