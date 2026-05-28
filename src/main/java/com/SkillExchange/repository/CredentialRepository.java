package com.SkillExchange.repository;

import com.SkillExchange.model.Credential;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface CredentialRepository extends MongoRepository<Credential, String> {
    List<Credential> findByUserId(String userId);
}