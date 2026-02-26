package com.SkillExchange.repository;

import java.util.Optional;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.SkillExchange.model.BaseUser;

@Repository
public interface BaseUserRepository extends MongoRepository<BaseUser, String> {
    Optional<BaseUser> findByEmail(String email);

    // ✅ New: Finds users where skillsOffered list contains the skill (Case-Insensitive)
    List<BaseUser> findBySkillsOfferedContainingIgnoreCase(String skill);
}