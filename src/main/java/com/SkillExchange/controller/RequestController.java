package com.SkillExchange.controller;

import com.SkillExchange.model.SkillRequest;
import com.SkillExchange.repository.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
@CrossOrigin(origins = "http://localhost:5173")
public class RequestController {

    @Autowired
    private RequestRepository requestRepository;

    // ✅ SEND A REQUEST (User 1 initiates)
    @PostMapping("/send")
    public ResponseEntity<SkillRequest> sendRequest(@RequestBody SkillRequest request) {
        // Force status to PENDING on new requests
        request.setStatus("PENDING");

        // 📝 DEBUG PRINT: Check your IDE console/terminal to see if these names are arriving!
        System.out.println("--- New Request Received ---");
        System.out.println("Sender Name: " + request.getSenderName());
        System.out.println("Receiver Name: " + request.getReceiverName());
        System.out.println("---------------------------");

        SkillRequest saved = requestRepository.save(request);
        return ResponseEntity.ok(saved);
    }

    // ✅ GET REQUESTS FOR ME (User 2 sees these to Accept/Reject)
    @GetMapping("/my-requests/{userId}")
    public ResponseEntity<List<SkillRequest>> getMyRequests(@PathVariable String userId) {
        List<SkillRequest> requests = requestRepository.findByReceiverId(userId);
        return ResponseEntity.ok(requests);
    }

    // ✅ GET REQUESTS I SENT (User 1 sees these to track progress)
    @GetMapping("/sent/{userId}")
    public ResponseEntity<List<SkillRequest>> getSentRequests(@PathVariable String userId) {
        List<SkillRequest> sent = requestRepository.findBySenderId(userId);
        return ResponseEntity.ok(sent);
    }

    // ✅ ACCEPT REQUEST
    @PutMapping("/accept/{id}")
    public ResponseEntity<SkillRequest> acceptRequest(@PathVariable String id) {
        return requestRepository.findById(id).map(req -> {
            req.setStatus("ACCEPTED");
            return ResponseEntity.ok(requestRepository.save(req));
        }).orElse(ResponseEntity.notFound().build());
    }
    
    // ✅ REJECT REQUEST
    @PutMapping("/reject/{id}")
    public ResponseEntity<SkillRequest> rejectRequest(@PathVariable String id) {
        return requestRepository.findById(id).map(req -> {
            req.setStatus("REJECTED");
            return ResponseEntity.ok(requestRepository.save(req));
        }).orElse(ResponseEntity.notFound().build());
    }
}