package com.SkillExchange.repository;

import com.SkillExchange.model.Skill;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface SkillRepository extends MongoRepository<Skill, String> {
    
    // Keep your existing query
    Optional<Skill> findBySkillName(String skillName);

    // ADD THESE:
    
    // 1. Get all skills for a specific user (to show the list on the Profile page)
    List<Skill> findByUserId(String userId);

    // 2. Count skills by user and type (to show the numbers on the Dashboard)
    // type will be either "OFFERED" or "WANTED"
    long countByUserIdAndType(String userId, String type);
}