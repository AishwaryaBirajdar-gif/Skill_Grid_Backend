// File: com.SkillExchange.service.SuggestionService.java
package com.SkillExchange.service;

import com.SkillExchange.model.BaseUser;
import com.SkillExchange.repository.BaseUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections; // Added for safety

@Service
public class SuggestionService {

    @Autowired
    private BaseUserRepository userRepository;

    public List<BaseUser> getSmartMatches(String userId) {
        // Find current user or throw custom exception
        BaseUser currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Use Optional to handle potential null lists from DB
        List<String> myOffers = currentUser.getSkillsOffered() != null ? currentUser.getSkillsOffered() : Collections.emptyList();
        List<String> myWants = currentUser.getSkillsWanted() != null ? currentUser.getSkillsWanted() : Collections.emptyList();

        // 2. Fetch all other users using the new repository method
        List<BaseUser> allOthers = userRepository.findAllByIdNot(userId);

        // 3. Double-Intersection Logic
        return allOthers.stream().filter(otherUser -> {
            List<String> theirOffers = otherUser.getSkillsOffered() != null ? otherUser.getSkillsOffered() : Collections.emptyList();
            List<String> theirWants = otherUser.getSkillsWanted() != null ? otherUser.getSkillsWanted() : Collections.emptyList();

            boolean theyHaveWhatIWant = theirOffers.stream()
                    .anyMatch(skill -> myWants.contains(skill));

            boolean theyWantWhatIHave = theirWants.stream()
                    .anyMatch(skill -> myOffers.contains(skill));

            return theyHaveWhatIWant && theyWantWhatIHave;
        }).collect(Collectors.toList());
    }
}