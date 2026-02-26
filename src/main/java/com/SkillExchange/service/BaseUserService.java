package com.SkillExchange.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.SkillExchange.model.BaseUser;
import com.SkillExchange.repository.BaseUserRepository;

import java.util.Optional;

@Service
public class BaseUserService {

    @Autowired
    private BaseUserRepository baseUserRepository;

    public Optional<BaseUser> findByEmail(String email) {
        return baseUserRepository.findByEmail(email);
    }

    public BaseUser save(BaseUser user) {
        return baseUserRepository.save(user);
    }

    public Optional<BaseUser> findById(String id) {
        return baseUserRepository.findById(id);
    }
}
