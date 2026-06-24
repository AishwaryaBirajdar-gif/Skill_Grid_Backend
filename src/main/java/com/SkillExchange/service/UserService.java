package com.SkillExchange.service;

import com.SkillExchange.model.BaseUser;
import com.SkillExchange.model.User;
import com.SkillExchange.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // --- Merged methods from BaseUserService ---
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User save(User user) {
        return userRepository.save(user);
    }
    
    // --- Existing UserService methods ---

    public List<User> searchUsersBySkill(String skill) {
        return userRepository.findBySkillsOfferedContainingIgnoreCase(skill);
    }

    // Note: If you have logic that uses BaseUser, you can pass it to this method
    public User createUser(BaseUser baseUser, User userDetails) {
        User user = new User(baseUser);
        user.setUsername(userDetails.getUsername());
        user.setBio(userDetails.getBio());
        user.setLocation(userDetails.getLocation());
        user.setTimezone(userDetails.getTimezone());
        user.setLanguages(userDetails.getLanguages());
        user.setProfilePic(userDetails.getProfilePic());
        
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

        if (userDetails.getName() != null) user.setName(userDetails.getName());
        if (userDetails.getBio() != null) user.setBio(userDetails.getBio());
        if (userDetails.getLocation() != null) user.setLocation(userDetails.getLocation());
        if (userDetails.getProfilePic() != null) user.setProfilePic(userDetails.getProfilePic());
        if (userDetails.getAvaliable() != null) user.setAvaliable(userDetails.getAvaliable());

        if (userDetails.getSkillsOffered() != null) {
            user.setSkillsOffered(userDetails.getSkillsOffered());
        }
        if (userDetails.getSkillsWanted() != null) {
            user.setSkillsWanted(userDetails.getSkillsWanted());
        }

        user.setKarmaPoints(userDetails.getKarmaPoints());
        user.setTrustScore(userDetails.getTrustScore());

        return userRepository.save(user);
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }
}