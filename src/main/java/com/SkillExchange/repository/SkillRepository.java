package com.SkillExchange.repository;

import com.SkillExchange.model.Skill;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface SkillRepository extends MongoRepository<Skill, String> {
    
    Optional<Skill> findBySkillName(String skillName);

    List<Skill> findByUserId(String userId);

    long countByUserIdAndType(String userId, String type);

    // ADDED: This method allows the service to filter skills by their status
    List<Skill> findByStatus(String status);
}