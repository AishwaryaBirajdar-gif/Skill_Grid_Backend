package com.SkillExchange.controller;

import com.SkillExchange.model.SkillSuggestion;
import com.SkillExchange.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "http://localhost:5173") 
public class AIPathController {

    @Autowired
    private RecommendationService recommendationService;

    // This is the missing piece that handles /api/ai/suggest?skill=...
    @GetMapping("/suggest") 
    public SkillSuggestion getSuggestion(@RequestParam String skill) {
        return recommendationService.getRecommendedPath(skill);
    }
}