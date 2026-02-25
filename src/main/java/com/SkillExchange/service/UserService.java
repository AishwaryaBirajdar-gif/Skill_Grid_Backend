package com.SkillExchange.service;

import com.SkillExchange.model.BaseUser;
import com.SkillExchange.model.User;
import com.SkillExchange.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // ✅ STEP 2 Logic: Search for users offering a specific skill
    public List<User> searchUsersBySkill(String skill) {
        return userRepository.findBySkillsOfferedContainingIgnoreCase(skill);
    }

    public User createUser(BaseUser baseUser, User userDetails) {
        User user = new User(baseUser);
        user.setUsername(userDetails.getUsername());
        user.setBio(userDetails.getBio());
        user.setLocation(userDetails.getLocation());
        user.setTimezone(userDetails.getTimezone());
        user.setLanguages(userDetails.getLanguages());
        user.setProfilePic(userDetails.getProfilePic());
        
        // Initialize lists to avoid null pointers
        user.setSkillsOffered(userDetails.getSkillsOffered());
        user.setSkillsWanted(userDetails.getSkillsWanted());

        return userRepository.save(user);
    }

    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(String id, User userDetails) {
        User user = getUserById(id);

        // Update basic info
        if (userDetails.getName() != null) user.setName(userDetails.getName());
        if (userDetails.getBio() != null) user.setBio(userDetails.getBio());
        if (userDetails.getLocation() != null) user.setLocation(userDetails.getLocation());
        if (userDetails.getProfilePic() != null) user.setProfilePic(userDetails.getProfilePic());
        if (userDetails.getAvaliable() != null) user.setAvaliable(userDetails.getAvaliable());

        // ✅ IMPORTANT: Update the Skill Lists for Bartering
        if (userDetails.getSkillsOffered() != null) {
            user.setSkillsOffered(userDetails.getSkillsOffered());
        }
        if (userDetails.getSkillsWanted() != null) {
            user.setSkillsWanted(userDetails.getSkillsWanted());
        }

        // Maintain points
        user.setKarmaPoints(userDetails.getKarmaPoints());
        user.setTrustScore(userDetails.getTrustScore());

        return userRepository.save(user);
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }
}