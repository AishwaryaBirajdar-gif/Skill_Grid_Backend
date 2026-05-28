package com.SkillExchange.controller;

import com.SkillExchange.DTO.SkillResponse;
import com.SkillExchange.model.Skill;
import com.SkillExchange.service.SkillAIService;
import com.SkillExchange.service.SkillService;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") 
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @Autowired
    private SkillAIService service;

    // ✅ FIXED: Extracts description and passes it directly to our local engine
    @PostMapping("/analyze")
    public SkillResponse analyze(@RequestBody String rawJsonBody) {
        try {
            JSONObject payload = new JSONObject(rawJsonBody);
            String targetDescription = payload.optString("description", "");
            String skillName = payload.optString("skillName", "Technology");
            
            // We pass BOTH the description and the name to our robust local service
            return service.analyzeLocalSkill(skillName, targetDescription);
        } catch (Exception e) {
            return service.analyzeLocalSkill("Technology", rawJsonBody);
        }
    }

    @PostMapping
    public ResponseEntity<Skill> createSkill(@RequestBody Skill skill) {
        try {
            Skill savedSkill = skillService.createSkill(skill);
            return new ResponseEntity<>(savedSkill, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/counts/{userId}")
    public ResponseEntity<Object> getSkillCounts(@PathVariable String userId) {
        return ResponseEntity.ok(skillService.getSkillCounts(userId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Skill>> getSkillsByUserId(@PathVariable String userId) {
        List<Skill> skills = skillService.getSkillsByOwnerId(userId);
        return ResponseEntity.ok(skills);
    }

    @GetMapping
    public ResponseEntity<List<Skill>> getAllSkills() {
        return ResponseEntity.ok(skillService.getAllSkills());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Skill> getSkillById(@PathVariable String id) {
        return skillService.getSkillById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Skill> updateSkill(@PathVariable String id, @RequestBody Skill updatedSkill) {
        Skill skill = skillService.updateSkill(id, updatedSkill);
        return skill != null ? ResponseEntity.ok(skill) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSkill(@PathVariable String id) {
        boolean deleted = skillService.deleteSkill(id);
        if (deleted) {
            return ResponseEntity.ok("Skill deleted successfully!");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Skill not found!");
    }

    @DeleteMapping("/user/{userId}/{skillName}")
    public ResponseEntity<String> deleteSkillByUserAndName(
            @PathVariable String userId, 
            @PathVariable String skillName) {
        boolean deleted = skillService.deleteByUserIdAndSkillName(userId, skillName);
        if (deleted) {
            return ResponseEntity.ok("Skill removed from profile!");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Skill not found for this user!");
    }
}