package com.SkillExchange.repository;

import com.SkillExchange.model.BaseUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface BaseUserRepository extends MongoRepository<BaseUser, String> {
    Optional<BaseUser> findByEmail(String email);
    List<BaseUser> findBySkillsOfferedContainingIgnoreCase(String skill);
    
    // This method is required for the service logic above
    List<BaseUser> findAllByIdNot(String id); 
}