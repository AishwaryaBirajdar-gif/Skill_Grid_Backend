package com.SkillExchange.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.SkillExchange.model.BaseUser;
import com.SkillExchange.model.User;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    List<User> findBySkillsOfferedContainingIgnoreCase(String skill);
    // This method is required for the service logic above
    List<BaseUser> findAllByIdNot(String id); 

    // ✅ ADDED FOR ADMIN DASHBOARD: Fetch users with at least one pending skill
    @Query("{ 'detailedSkillsOffered.level': 'PENDING' }")
    List<User> findUsersWithPendingSkills();
}