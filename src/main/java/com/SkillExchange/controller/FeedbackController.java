package com.SkillExchange.controller;

import com.SkillExchange.model.Feedback;
import com.SkillExchange.repository.FeedbackRepository;
import com.SkillExchange.service.RequestService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin("*") // Allows your React app to talk to this controller
public class FeedbackController {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private RequestService requestService;

    @PostMapping("/{requestId}/submit")
    public ResponseEntity<?> submitFeedback(@PathVariable String requestId, @RequestBody Feedback feedback) {
        // 1. Save the actual review to the feedback collection
        feedbackRepository.save(feedback);

        // 2. Update the SkillRequest to track that this user is done
        requestService.markFeedbackDone(requestId, feedback.getFromUserId());

        return ResponseEntity.ok("Feedback submitted successfully!");
    }
    
    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<Map<String, Object>> getUserRatingSummary(@PathVariable String userId) {
        List<Feedback> reviews = feedbackRepository.findByToUserId(userId);
        
        double average = reviews.stream()
                .mapToInt(Feedback::getRating)
                .average()
                .orElse(0.0);

        Map<String, Object> response = new HashMap<>();
        response.put("averageRating", Math.round(average * 10) / 10.0);
        response.put("totalReviews", reviews.size());
        response.put("reviews", reviews);
        
        return ResponseEntity.ok(response);
    }
}