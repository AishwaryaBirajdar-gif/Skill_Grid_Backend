package com.SkillExchange.controller;

import com.SkillExchange.model.SkillRequest;
import com.SkillExchange.model.User;
import com.SkillExchange.repository.RequestRepository;
import com.SkillExchange.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/requests")
@CrossOrigin(origins = "http://localhost:5173")
public class RequestController {

    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private UserRepository userRepository;

    // --- FETCH & SEND METHODS ---

    @PostMapping("/send")
    public ResponseEntity<SkillRequest> sendRequest(@RequestBody SkillRequest skillRequest) {
        skillRequest.setStatus("PENDING");
        skillRequest.setSenderProgress(0);
        skillRequest.setReceiverProgress(0);
        skillRequest.setSenderLocked(false);
        skillRequest.setReceiverLocked(false);
        return ResponseEntity.ok(requestRepository.save(skillRequest));
    }

    @GetMapping("/my-requests/{userId}")
    public ResponseEntity<List<SkillRequest>> getMyRequests(@PathVariable String userId) {
        return ResponseEntity.ok(requestRepository.findByReceiverId(userId));
    }

    @GetMapping("/sent/{userId}")
    public ResponseEntity<List<SkillRequest>> getSentRequests(@PathVariable String userId) {
        return ResponseEntity.ok(requestRepository.findBySenderId(userId));
    }

    @PutMapping("/accept/{requestId}")
    public ResponseEntity<SkillRequest> acceptRequest(@PathVariable String requestId) {
        SkillRequest request = requestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("Not found"));
        request.setStatus("ACCEPTED");
        return ResponseEntity.ok(requestRepository.save(request));
    }

    // --- WORKFLOW METHODS (SUBMIT & VERIFY) ---

    @PutMapping("/{requestId}/submit-work")
    public ResponseEntity<SkillRequest> submitWork(@PathVariable String requestId, @RequestParam String userId) {
        SkillRequest request = requestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("Not found"));
        
        // Check which user is submitting and apply the 25% boost + Lock
        if (userId.equals(request.getSenderId())) {
            if(request.isSenderLocked()) return ResponseEntity.badRequest().body(null); // Prevent double submission
            request.setSenderProgress(Math.min(100, request.getSenderProgress() + 25));
            request.setSenderLocked(true); // 🔒 Lock: they must wait for verification
        } else {
            if(request.isReceiverLocked()) return ResponseEntity.badRequest().body(null);
            request.setReceiverProgress(Math.min(100, request.getReceiverProgress() + 25));
            request.setReceiverLocked(true); // 🔒 Lock
        }
        
        return ResponseEntity.ok(requestRepository.save(request));
    }

    @PutMapping("/{requestId}/verify-work")
    public ResponseEntity<SkillRequest> verifyWork(@PathVariable String requestId, @RequestParam String verifierId) {
        SkillRequest request = requestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("Not found"));
        
        // If Verifier is Receiver, they are unlocking the Sender
        if (verifierId.equals(request.getReceiverId())) {
            request.setSenderLocked(false); // 🔓 Unlock
        } else {
            request.setReceiverLocked(false); // 🔓 Unlock
        }

        // ✅ AUTO-COMPLETE: If both hit 100%, finish the barter and transfer Karma
        if (request.getSenderProgress() == 100 && request.getReceiverProgress() == 100) {
            request.setStatus("COMPLETED");
            transferKarma(request, 50); 
        }
        
        return ResponseEntity.ok(requestRepository.save(request));
    }

    // --- EXISTING NEGOTIATION METHODS ---

    @PutMapping("/{requestId}/propose-requirements")
    public ResponseEntity<SkillRequest> propose(@PathVariable String requestId, @RequestBody Map<String, List<String>> payload) {
        SkillRequest request = requestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("Not found"));
        if (payload.containsKey("senderRequirements")) {
            request.setSenderRequirements(payload.get("senderRequirements"));
            request.setSenderRequirementsApproved(false);
        }
        if (payload.containsKey("receiverRequirements")) {
            request.setReceiverRequirements(payload.get("receiverRequirements"));
            request.setReceiverRequirementsApproved(false);
        }
        return ResponseEntity.ok(requestRepository.save(request));
    }

    @PutMapping("/{requestId}/approve-track")
    public ResponseEntity<SkillRequest> approveTrack(@PathVariable String requestId, @RequestParam String userId) {
        SkillRequest request = requestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("Not found"));
        if (userId.equals(request.getSenderId())) request.setSenderRequirementsApproved(true);
        else request.setReceiverRequirementsApproved(true);

        if (request.isSenderRequirementsApproved() && request.isReceiverRequirementsApproved()) {
            request.setStatus("ACTIVE");
        }
        return ResponseEntity.ok(requestRepository.save(request));
    }

    private void transferKarma(SkillRequest request, int amount) {
        User sender = userRepository.findById(request.getSenderId()).orElse(null);
        User receiver = userRepository.findById(request.getReceiverId()).orElse(null);
        if (sender != null && receiver != null) {
            receiver.setKarmaPoints(receiver.getKarmaPoints() - amount);
            sender.setKarmaPoints(sender.getKarmaPoints() + amount);
            userRepository.save(sender); 
            userRepository.save(receiver);
        }
    }
}