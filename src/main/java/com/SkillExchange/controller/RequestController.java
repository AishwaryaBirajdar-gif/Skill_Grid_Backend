package com.SkillExchange.controller;

import com.SkillExchange.model.SkillRequest;
import com.SkillExchange.model.SkillRequest.Milestone;
import com.SkillExchange.model.User;
import com.SkillExchange.repository.SkillRequestRepository;
import com.SkillExchange.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/requests")
@CrossOrigin(origins = "http://localhost:5173")
public class RequestController {

    @Autowired
    private SkillRequestRepository requestRepository;
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private com.SkillExchange.repository.FeedbackRepository feedbackRepository;
    
    @Autowired
    private com.SkillExchange.service.RequestService requestService;

    @GetMapping("/{requestId}")
    public ResponseEntity<SkillRequest> getRequestById(@PathVariable String requestId) {
        return ResponseEntity.ok(requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request Room not found")));
    }

    @GetMapping("/my-requests/{userId}")
    public ResponseEntity<List<SkillRequest>> getMyRequests(@PathVariable String userId) {
        return ResponseEntity.ok(requestRepository.findByReceiverId(userId));
    }

    @GetMapping("/sent/{userId}")
    public ResponseEntity<List<SkillRequest>> getSentRequests(@PathVariable String userId) {
        return ResponseEntity.ok(requestRepository.findBySenderId(userId));
    }

    @PostMapping("/send")
    public ResponseEntity<SkillRequest> sendRequest(@RequestBody SkillRequest skillRequest) {
        skillRequest.setStatus("PENDING");
        skillRequest.setSenderProgress(0);
        skillRequest.setReceiverProgress(0);
        skillRequest.setSenderLocked(false);
        skillRequest.setReceiverLocked(false);
        return ResponseEntity.ok(requestRepository.save(skillRequest));
    }

    @PutMapping("/accept/{requestId}")
    public ResponseEntity<SkillRequest> acceptRequest(@PathVariable String requestId) {
        SkillRequest request = requestRepository.findById(requestId).orElseThrow();
        request.setStatus("ACCEPTED");

        if ("KARMA_PAYMENT".equals(request.getSkillOffered())) {
            // User 1 (Sender) is paying karma, nothing to teach.
            request.setSenderRequirementsApproved(true); 
            request.setSenderProgress(100); 
        }
        
        return ResponseEntity.ok(requestRepository.save(request));
    }

    @PutMapping("/{requestId}/propose-requirements")
    public ResponseEntity<SkillRequest> propose(@PathVariable String requestId, @RequestBody Map<String, List<String>> payload) {
        SkillRequest request = requestRepository.findById(requestId).orElseThrow();
        
        if (payload.containsKey("senderRequirements")) {
            List<String> requirements = payload.get("senderRequirements");
            request.setSenderRequirements(requirements);
            request.setMilestones(convertToMilestones(requirements, request.getTotalKarmaPrice()));
            request.setSenderRequirementsApproved(false);
            request.setSenderReqNeedsUpdate(false);
        }
        
        if (payload.containsKey("receiverRequirements")) {
            List<String> requirements = payload.get("receiverRequirements");
            request.setReceiverRequirements(requirements);
            request.setReceiverRequirementsApproved(false);
            request.setReceiverReqNeedsUpdate(false);
        }
        
        return ResponseEntity.ok(requestRepository.save(request));
    }

    private List<Milestone> convertToMilestones(List<String> titles, int totalPrice) {
        List<Milestone> milestones = new ArrayList<>();
        if (titles == null || titles.isEmpty()) return milestones;
        
        int karmaPerStep = titles.size() > 0 ? totalPrice / titles.size() : 0;
        for (String title : titles) {
            Milestone m = new Milestone();
            m.setId(UUID.randomUUID().toString());
            m.setTitle(title);
            m.setStatus("PENDING");
            m.setKarmaValue(karmaPerStep);
            milestones.add(m);
        }
        return milestones;
    }

    @PutMapping("/{requestId}/send-back")
    public ResponseEntity<SkillRequest> sendBack(@PathVariable String requestId, @RequestParam String trackType) {
        SkillRequest request = requestRepository.findById(requestId).orElseThrow();
        
        if ("sender".equals(trackType)) {
            request.setSenderRequirementsApproved(false);
            request.setSenderReqNeedsUpdate(true);
        } else if ("receiver".equals(trackType)) {
            request.setReceiverRequirementsApproved(false);
            request.setReceiverReqNeedsUpdate(true);
        }
        
        return ResponseEntity.ok(requestRepository.save(request));
    }
    
    @PutMapping("/{requestId}/approve-track")
    public ResponseEntity<SkillRequest> approveTrack(
        @PathVariable String requestId, 
        @RequestParam String userId,
        @RequestParam String trackType // ✅ New Parameter
    ) {
        SkillRequest request = requestRepository.findById(requestId).orElseThrow();

        if ("sender".equals(trackType)) {
            request.setSenderRequirementsApproved(true);
        } else if ("receiver".equals(trackType)) {
            request.setReceiverRequirementsApproved(true);
        }

        // Auto-approve Karma side if applicable
        if ("KARMA_PAYMENT".equals(request.getSkillOffered())) {
            request.setSenderRequirementsApproved(true);
        }

        // THE GATE: Only move to ACTIVE if both are now true
        if (request.isSenderRequirementsApproved() && request.isReceiverRequirementsApproved()) {
            request.setStatus("ACTIVE");
        }

        return ResponseEntity.ok(requestRepository.save(request));
    }

    @PutMapping("/{requestId}/submit-work")
    public ResponseEntity<SkillRequest> submitWork(@PathVariable String requestId, @RequestParam String userId) {
        SkillRequest request = requestRepository.findById(requestId).orElseThrow();
        if (userId.equals(request.getSenderId())) {
            request.setSenderLocked(true);
        } else {
            request.setReceiverLocked(true);
        }
        return ResponseEntity.ok(requestRepository.save(request));
    }

 // Replace your verifyWork and transferKarma methods with these:

    @PutMapping("/{requestId}/verify-work")
    public ResponseEntity<SkillRequest> verifyWork(@PathVariable String requestId, @RequestParam String verifierId) {
        SkillRequest request = requestRepository.findById(requestId).orElseThrow();
        
        if (verifierId.equals(request.getReceiverId())) {
            request.setSenderLocked(false); 
            int total = (request.getMilestones() != null) ? request.getMilestones().size() : 1;
            int step = 100 / Math.max(1, total);
            request.setSenderProgress(Math.min(100, request.getSenderProgress() + step));
        } 
        else {
            request.setReceiverLocked(false); 
            int totalReqs = (request.getReceiverRequirements() != null) ? request.getReceiverRequirements().size() : 1;
            int step = 100 / Math.max(1, totalReqs);
            request.setReceiverProgress(Math.min(100, request.getReceiverProgress() + step));

            if ("KARMA_PAYMENT".equals(request.getSkillOffered())) {
                int totalValue = request.getTotalKarmaPrice();
                int karmaPerStep = totalValue / Math.max(1, totalReqs);
                // ✅ Matches the updated signature below
                transferKarma(request.getSenderId(), request.getReceiverId(), karmaPerStep);
            }
        }

        if (request.getSenderProgress() >= 100 && request.getReceiverProgress() >= 100) {
            request.setStatus("COMPLETED");
        }
        
        return ResponseEntity.ok(requestRepository.save(request));
    }

    // ✅ FIXED SIGNATURE: Matches the call site (String, String, int)
    private void transferKarma(String senderId, String receiverId, int amount) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        if (sender.getKarmaPoints() < amount) {
            throw new RuntimeException("Insufficient Karma points!");
        }

        sender.setKarmaPoints(sender.getKarmaPoints() - amount);
        receiver.setKarmaPoints(receiver.getKarmaPoints() + amount);

        userRepository.save(sender); 
        userRepository.save(receiver);
    }
}