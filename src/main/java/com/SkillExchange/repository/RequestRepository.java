package com.SkillExchange.repository;

import com.SkillExchange.model.SkillRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface RequestRepository extends MongoRepository<SkillRequest, String> {
    List<SkillRequest> findByReceiverId(String receiverId);
    List<SkillRequest> findBySenderId(String senderId);
}