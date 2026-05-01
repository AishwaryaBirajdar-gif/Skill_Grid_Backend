package com.SkillExchange.service;

import com.SkillExchange.model.BaseUser;
import com.SkillExchange.model.Skill;
import com.SkillExchange.repository.BaseUserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SuggestionService {

    @Autowired
    private BaseUserRepository userRepository;

    public List<BaseUser> getSmartMatches(String userId) {

        // Find current user
        BaseUser currentUser = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found with ID: " + userId));

        // My offered skills
        List<Skill> myOffers =
                currentUser.getSkillsOffered() != null
                        ? currentUser.getSkillsOffered()
                        : Collections.emptyList();

        // My wanted skills
        List<Skill> myWants =
                currentUser.getSkillsWanted() != null
                        ? currentUser.getSkillsWanted()
                        : Collections.emptyList();

        // Convert to lowercase names for comparison
        Set<String> myOfferNames = myOffers.stream()
                .map(skill -> skill.getSkillName().toLowerCase())
                .collect(Collectors.toSet());

        Set<String> myWantNames = myWants.stream()
                .map(skill -> skill.getSkillName().toLowerCase())
                .collect(Collectors.toSet());

        // Fetch all other users
        List<BaseUser> allOthers =
                userRepository.findAllByIdNot(userId);

        // Smart barter matching
        return allOthers.stream().filter(otherUser -> {

            List<Skill> theirOffers =
                    otherUser.getSkillsOffered() != null
                            ? otherUser.getSkillsOffered()
                            : Collections.emptyList();

            List<Skill> theirWants =
                    otherUser.getSkillsWanted() != null
                            ? otherUser.getSkillsWanted()
                            : Collections.emptyList();

            // Convert to names
            Set<String> theirOfferNames = theirOffers.stream()
                    .map(skill -> skill.getSkillName().toLowerCase())
                    .collect(Collectors.toSet());

            Set<String> theirWantNames = theirWants.stream()
                    .map(skill -> skill.getSkillName().toLowerCase())
                    .collect(Collectors.toSet());

            // They offer what I want
            boolean theyHaveWhatIWant =
                    theirOfferNames.stream()
                            .anyMatch(myWantNames::contains);

            // They want what I offer
            boolean theyWantWhatIHave =
                    theirWantNames.stream()
                            .anyMatch(myOfferNames::contains);

            return theyHaveWhatIWant && theyWantWhatIHave;

        }).collect(Collectors.toList());
    }
}