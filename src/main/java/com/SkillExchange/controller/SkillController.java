package com.SkillExchange.controller;

import com.SkillExchange.model.Skill;
import com.SkillExchange.service.SkillService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/skills")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") 
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    // ✅ POST: Create a Skill
    @PostMapping
    public ResponseEntity<Skill> createSkill(@RequestBody Skill skill) {
        try {
            Skill savedSkill = skillService.createSkill(skill);
            return new ResponseEntity<>(savedSkill, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // ✅ GET: Fetch counts for Dashboard
    @GetMapping("/counts/{userId}")
    public ResponseEntity<Object> getSkillCounts(@PathVariable String userId) {
        return ResponseEntity.ok(skillService.getSkillCounts(userId));
    }

    // ✅ GET: All skills for a specific user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Skill>> getSkillsByUserId(@PathVariable String userId) {
        List<Skill> skills = skillService.getSkillsByOwnerId(userId);
        return ResponseEntity.ok(skills);
    }

    // ✅ GET: All skills in the system
    @GetMapping
    public ResponseEntity<List<Skill>> getAllSkills() {
        return ResponseEntity.ok(skillService.getAllSkills());
    }

    // ✅ GET: Single skill by ID
    @GetMapping("/{id}")
    public ResponseEntity<Skill> getSkillById(@PathVariable String id) {
        return skillService.getSkillById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ PUT: Update a skill
    @PutMapping("/{id}")
    public ResponseEntity<Skill> updateSkill(@PathVariable String id, @RequestBody Skill updatedSkill) {
        Skill skill = skillService.updateSkill(id, updatedSkill);
        return skill != null ? ResponseEntity.ok(skill) : ResponseEntity.notFound().build();
    }

    // ✅ DELETE: Remove a skill by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSkill(@PathVariable String id) {
        boolean deleted = skillService.deleteSkill(id);
        if (deleted) {
            return ResponseEntity.ok("Skill deleted successfully!");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Skill not found!");
    }

    // ✅ NEW DELETE: Remove a skill by User ID and Skill Name
    // This handles the "X" button in your Profile.jsx
    @DeleteMapping("/user/{userId}/{skillName}")
    public ResponseEntity<String> deleteSkillByUserAndName(
            @PathVariable String userId, 
            @PathVariable String skillName) {
        
        // This logic searches for the specific skill string belonging to that user
        boolean deleted = skillService.deleteByUserIdAndSkillName(userId, skillName);
        
        if (deleted) {
            return ResponseEntity.ok("Skill removed from profile!");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Skill not found for this user!");
    }
}