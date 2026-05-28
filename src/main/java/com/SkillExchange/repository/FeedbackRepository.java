package com.SkillExchange.repository;

import com.SkillExchange.model.Feedback;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends MongoRepository<Feedback, String> {
    List<Feedback> findByToUserId(String toUserId);
}