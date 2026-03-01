package com.SkillExchange.controller;

import com.SkillExchange.model.BaseUser;
import com.SkillExchange.service.SuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller to handle Smart Matching suggestions for skill bartering.
 * Matches the /api/user/suggestions/{userId} pattern used in the frontend Dashboard.
 */
@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:5173") // Matches your Vite/React default port 
public class SuggestionController {

    @Autowired
    private SuggestionService suggestionService;

    /**
     * GET /api/user/suggestions/{userId}
     * Returns a list of users who offer what the current user wants 
     * AND want what the current user offers.
     */
    @GetMapping("/suggestions/{userId}")
    public ResponseEntity<List<BaseUser>> getSmartMatches(@PathVariable String userId) {
        try {
            List<BaseUser> matches = suggestionService.getSmartMatches(userId);
            return ResponseEntity.ok(matches);
        } catch (RuntimeException e) {
            // Returns a 404 if the specific user ID isn't found in the database [cite: 31]
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            // Generic error handling for unexpected backend issues
            return ResponseEntity.internalServerError().build();
        }
    }
}