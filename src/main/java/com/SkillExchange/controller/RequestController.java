package com.SkillExchange.controller;

import com.SkillExchange.model.SkillRequest;
import com.SkillExchange.model.SkillRequest.Milestone;
import com.SkillExchange.model.User;
import com.SkillExchange.repository.RequestRepository;
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
    private RequestRepository requestRepository;
    @Autowired
    private UserRepository userRepository;

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
        return ResponseEntity.ok(requestRepository.save(request));
    }

    /**
     * Handles proposing requirements for both sender and receiver tracks.
     * Clears "NeedsUpdate" flags upon successful submission.
     */
    @PutMapping("/{requestId}/propose-requirements")
    public ResponseEntity<SkillRequest> propose(@PathVariable String requestId, @RequestBody Map<String, List<String>> payload) {
        SkillRequest request = requestRepository.findById(requestId).orElseThrow();
        
        if (payload.containsKey("senderRequirements")) {
            List<String> requirements = payload.get("senderRequirements");
            request.setSenderRequirements(requirements);
            request.setMilestones(convertToMilestones(requirements, request.getTotalKarmaPrice()));
            request.setSenderRequirementsApproved(false);
            request.setSenderReqNeedsUpdate(false); // Reset flag on update
        }
        
        if (payload.containsKey("receiverRequirements")) {
            List<String> requirements = payload.get("receiverRequirements");
            request.setReceiverRequirements(requirements);
            // If receiver track also uses milestones, generate them here similarly
            request.setReceiverRequirementsApproved(false);
            request.setReceiverReqNeedsUpdate(false); // Reset flag on update
        }
        
        return ResponseEntity.ok(requestRepository.save(request));
    }

    private List<Milestone> convertToMilestones(List<String> titles, int totalPrice) {
        List<Milestone> milestones = new ArrayList<>();
        if (titles == null || titles.isEmpty()) return milestones;
        
        int karmaPerStep = totalPrice / titles.size();
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
            request.setSenderReqNeedsUpdate(true); // Signal to User A to edit
        } else if ("receiver".equals(trackType)) {
            request.setReceiverRequirementsApproved(false);
            request.setReceiverReqNeedsUpdate(true); // Signal to User B to edit
        }
        
        return ResponseEntity.ok(requestRepository.save(request));
    }

    @PutMapping("/{requestId}/approve-track")
    public ResponseEntity<SkillRequest> approveTrack(@PathVariable String requestId, @RequestParam String userId) {
        SkillRequest request = requestRepository.findById(requestId).orElseThrow();
        
        if (userId.equals(request.getSenderId())) {
            request.setSenderRequirementsApproved(true);
        } else {
            request.setReceiverRequirementsApproved(true);
        }

        // Both tracks must be approved for the room to go ACTIVE
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

    @PutMapping("/{requestId}/verify-work")
    public ResponseEntity<SkillRequest> verifyWork(@PathVariable String requestId, @RequestParam String verifierId) {
        SkillRequest request = requestRepository.findById(requestId).orElseThrow();
        boolean isKarmaBarter = "KARMA_PAYMENT".equals(request.getSkillOffered());

        if (verifierId.equals(request.getReceiverId())) {
            request.setSenderLocked(false); 
            int step = 100 / Math.max(1, request.getMilestones().size());
            request.setSenderProgress(Math.min(100, request.getSenderProgress() + step));
        } else {
            request.setReceiverLocked(false); 
            request.setReceiverProgress(Math.min(100, request.getReceiverProgress() + 20));
        }

        if (isKarmaBarter) {
            int amount = request.getTotalKarmaPrice() / Math.max(1, request.getMilestones().size());
            transferKarma(request, amount);
        }
        
        if (request.getSenderProgress() >= 100 && (isKarmaBarter || request.getReceiverProgress() >= 100)) {
            request.setStatus("COMPLETED");
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