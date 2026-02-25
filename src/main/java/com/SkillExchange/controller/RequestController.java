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

    // SEND A REQUEST
    @PostMapping("/send")
    public ResponseEntity<SkillRequest> sendRequest(@RequestBody SkillRequest request) {
        request.setStatus("PENDING");
        return ResponseEntity.ok(requestRepository.save(request));
    }

    // GET REQUESTS FOR ME (To show in a dashboard)
    @GetMapping("/my-requests/{userId}")
    public ResponseEntity<List<SkillRequest>> getMyRequests(@PathVariable String userId) {
        return ResponseEntity.ok(requestRepository.findByReceiverId(userId));
    }

    // ACCEPT REQUEST
    @PutMapping("/accept/{id}")
    public ResponseEntity<SkillRequest> acceptRequest(@PathVariable String id) {
        return requestRepository.findById(id).map(req -> {
            req.setStatus("ACCEPTED");
            return ResponseEntity.ok(requestRepository.save(req));
        }).orElse(ResponseEntity.notFound().build());
    }
}