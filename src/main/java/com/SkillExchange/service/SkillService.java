package com.SkillExchange.service;

import com.SkillExchange.DTO.SkillResponse;
import com.SkillExchange.model.Skill;
import com.SkillExchange.model.User;
import com.SkillExchange.repository.SkillRepository;
import com.SkillExchange.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SkillService {

    private final SkillRepository skillRepository;

    @Autowired private UserRepository userRepository;
    @Autowired private SkillAIService skillAIService;
    @Autowired private BaseUserService baseUserService;

    public SkillService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    // --- NEW METHODS ---
    public List<Skill> findAllPending() {
        return skillRepository.findByStatus("PENDING");
    }

    public void approveSkill(String id) {
        Skill skill = skillRepository.findById(id).orElseThrow(() -> new RuntimeException("Skill not found"));
        skill.setStatus("APPROVED");
        skillRepository.save(skill);
    }
    // -------------------

    public Skill createSkill(Skill skill) {
        if (skill.getUserId() == null || skill.getUserId().isEmpty()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
            skill.setUserId(user.getId());
        }

        try {
            String input = (skill.getDescription() != null && !skill.getDescription().isBlank()) ? skill.getDescription() : skill.getSkillName();
            SkillResponse aiResponse = skillAIService.analyzeSkill(input);
            skill.setDepthLevel(aiResponse != null && aiResponse.getLevel() != null ? aiResponse.getLevel() : "Beginner");
        } catch (Exception e) {
            skill.setDepthLevel("Beginner");
        }

        Skill savedSkill = skillRepository.save(skill);
        Optional<User> userOptional = userRepository.findById(skill.getUserId());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getSkillsOffered() == null) user.setSkillsOffered(new ArrayList<>());
            user.getSkillsOffered().add(savedSkill);
            user.refreshCounts();
            userRepository.save(user);
        }
        return savedSkill;
    }

    public List<Skill> getAllSkills() { return skillRepository.findAll(); }
    public Optional<Skill> getSkillById(String id) { return skillRepository.findById(id); }

    public Skill updateSkill(String id, Skill updatedSkill) {
        return skillRepository.findById(id).map(skill -> {
            if (updatedSkill.getSkillName() != null) skill.setSkillName(updatedSkill.getSkillName());
            if (updatedSkill.getCategory() != null) skill.setCategory(updatedSkill.getCategory());
            if (updatedSkill.getDescription() != null) skill.setDescription(updatedSkill.getDescription());
            skill.setTrending(updatedSkill.isTrending());
            return skillRepository.save(skill);
        }).orElse(null);
    }

    public boolean deleteSkill(String id) {
        if (skillRepository.existsById(id)) {
            skillRepository.deleteById(id);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
            if (user.getSkills() != null && user.getSkills().contains(id)) {
                user.getSkills().remove(id);
                userRepository.save(user);
            }
            return true;
        }
        return false;
    }

    public List<Skill> getSkillsByOwnerId(String userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        return userOptional.isEmpty() ? List.of() : userOptional.get().getSkillsOffered();
    }

    public boolean deleteByUserIdAndSkillName(String userId, String skillName) { return false; }
    public Object getSkillCounts(String userId) { return null; }
}