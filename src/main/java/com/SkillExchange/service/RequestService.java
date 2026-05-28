package com.SkillExchange.service;

import com.SkillExchange.model.SkillRequest;
import com.SkillExchange.repository.SkillRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class RequestService {

    @Autowired
    private SkillRequestRepository requestRepository;

    public void markFeedbackDone(String requestId, String userId) {
        SkillRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));

        // Initialize map if it doesn't exist
        if (request.getFeedbackStatus() == null) {
            request.setFeedbackStatus(new HashMap<>());
        }

        // Set this user's feedback as complete
        request.getFeedbackStatus().put(userId, true);

        // If both parties have submitted feedback, officially CLOSE the request
        if (request.getFeedbackStatus().size() >= 2) {
            request.setStatus("CLOSED");
        }

        requestRepository.save(request);
    }
}