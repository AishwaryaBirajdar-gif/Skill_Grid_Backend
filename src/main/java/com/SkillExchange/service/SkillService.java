package com.SkillExchange.service;

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
    private BaseUserService baseUserService; // to fetch BaseUser from email

    public SkillService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    // Create
    public Skill createSkill(Skill skill) {
        Skill savedSkill = skillRepository.save(skill);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        if (user.getSkills() == null) {
            user.setSkills(new ArrayList<>());
        }
        if (!user.getSkills().contains(savedSkill.getId())) {
            user.getSkills().add(savedSkill.getId());
        }
        userRepository.save(user);
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
	    // 1. Find the User using the provided userId
	    Optional<User> userOptional = userRepository.findById(userId); // Assuming findById exists in UserRepository

	    if (userOptional.isEmpty()) {
	        // Handle case where user does not exist
	        return List.of(); // Return an empty list
	    }

	    User user = userOptional.get();
	    
	    // 2. Get the list of skill IDs from the User object
	    List<String> skillIds = user.getSkills();

	    if (skillIds == null || skillIds.isEmpty()) {
	        return List.of(); // Return an empty list if the user has no skills listed
	    }

	    // 3. Fetch the actual Skill objects using the list of IDs
	    // Assuming SkillRepository extends MongoRepository or JpaRepository
	    // and that you can use findAllById(Iterable<ID> ids)
	    List<Skill> skills = skillRepository.findAllById(skillIds);
	    
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
