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

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SkillAIService skillAIService;
    

    @Autowired
    private BaseUserService baseUserService; // to fetch BaseUser from email


    public SkillService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

   
    // Create
    public Skill createSkill(Skill skill) {

        // ✅ IMPORTANT FIX
        // DO NOT overwrite frontend userId

        if (skill.getUserId() == null || skill.getUserId().isEmpty()) {

            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            String email = authentication.getName();

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() ->
                            new RuntimeException("User not found"));

            skill.setUserId(user.getId());
        }

        // ✅ AI DEPTH GENERATION
        try {

            String input =
                    (skill.getDescription() != null &&
                     !skill.getDescription().isBlank())
                            ? skill.getDescription()
                            : skill.getSkillName();

            SkillResponse aiResponse =
                    skillAIService.analyzeSkill(input);

            if (aiResponse != null &&
                    aiResponse.getLevel() != null) {

                skill.setDepthLevel(aiResponse.getLevel());

            } else {

                skill.setDepthLevel("Beginner");
            }

        } catch (Exception e) {

            System.out.println(
                    "AI depth generation failed: " + e.getMessage()
            );

            skill.setDepthLevel("Beginner");
        }

        // ✅ SAVE SKILL
        Skill savedSkill = skillRepository.save(skill);

        // ✅ FIND ACTUAL OWNER
        Optional<User> userOptional =
                userRepository.findById(skill.getUserId());

        if (userOptional.isPresent()) {

            User user = userOptional.get();

            if (user.getSkillsOffered() == null) {
                user.setSkillsOffered(new ArrayList<>());
            }

            user.getSkillsOffered().add(savedSkill);

            user.refreshCounts();

            userRepository.save(user);
        }

        return savedSkill;
    }
    // Get All
    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }

    // Get by ID
    public Optional<Skill> getSkillById(String id) {
        return skillRepository.findById(id);
    }

    // Update (only non-null fields)
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
            // Step 1: Delete from Skill repository
            skillRepository.deleteById(id);

            // Step 2: Get current user from SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            // Step 3: Remove the skill id from user's skills list
            if (user.getSkills() != null && user.getSkills().contains(id)) {
                user.getSkills().remove(id);
                userRepository.save(user);
            }

            return true;
        }
        return false;
    }


    public List<Skill> getSkillsByOwnerId(String userId) {

        // Find user
        Optional<User> userOptional = userRepository.findById(userId);

        // If user not found
        if (userOptional.isEmpty()) {
            return List.of();
        }

        User user = userOptional.get();

        // Get skills directly
        List<Skill> skills = user.getSkillsOffered();

        // Handle null
        if (skills == null || skills.isEmpty()) {
            return List.of();
        }

        return skills;
    }
	public boolean deleteByUserIdAndSkillName(String userId, String skillName) {
		// TODO Auto-generated method stub
		return false;
	}

	public Object getSkillCounts(String userId) {
		// TODO Auto-generated method stub
		return null;
	}
}
