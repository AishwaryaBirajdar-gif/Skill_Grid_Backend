package com.SkillExchange.controller;

import com.SkillExchange.model.*;
import com.SkillExchange.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/credentials")
@CrossOrigin(origins = "http://localhost:5173")
public class CredentialController {

    @Autowired private CredentialRepository credentialRepository;
    @Autowired private SkillRequestRepository requestRepository;
    @Autowired private FeedbackRepository feedbackRepository;

    @PostMapping("/generate/{requestId}")
    public ResponseEntity<?> generateBadge(@PathVariable String requestId, @RequestParam String userId) {
        
        // 1. Fetch Request Details
        SkillRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        // 2. Security Check: Only allow badges for COMPLETED barters
     // In CredentialController.java
        if (!"COMPLETED".equals(request.getStatus()) && !"CLOSED".equals(request.getStatus())) {
            return ResponseEntity.badRequest().body("Barter must be finished to claim a badge.");
        }

        // 3. Fetch Feedback to get the Peer Rating
        List<Feedback> feedbacks = feedbackRepository.findByToUserId(userId);
        // Find the specific feedback related to this request
        Feedback relevantFeedback = feedbacks.stream()
                .filter(f -> f.getRequestId().equals(requestId))
                .findFirst()
                .orElse(null);

        if (relevantFeedback == null) {
            return ResponseEntity.badRequest().body("Feedback is required before claiming a badge.");
        }

        // 4. Create the Credential (The Job-Ready Data)
        Credential cred = new Credential();
        cred.setUserId(userId);
        cred.setRequestId(requestId);
        cred.setSkillName(request.getSkillRequested());
        cred.setPartnerName(userId.equals(request.getSenderId()) ? request.getReceiverName() : request.getSenderName());
        cred.setFinalRating(relevantFeedback.getRating());
        cred.setFeedbackComment(relevantFeedback.getComment());
        cred.setMilestonesCompleted(5); // You can link this to your actual milestone count
        
        // Generate Professional Metadata
        cred.setIssueDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        cred.setUniqueSerial("SG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        Credential saved = credentialRepository.save(cred);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Credential>> getUserBadges(@PathVariable String userId) {
        return ResponseEntity.ok(credentialRepository.findByUserId(userId));
    }
}