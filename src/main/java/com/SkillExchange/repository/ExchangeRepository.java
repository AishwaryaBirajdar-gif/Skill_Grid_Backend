package com.SkillExchange.repository;

import com.SkillExchange.model.Exchange;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ExchangeRepository extends MongoRepository<Exchange, String> {
    List<Exchange> findByStatus(String status);
    
    // Changed to use startDate since that is the field in your Exchange model
    List<Exchange> findTop10ByOrderByStartDateDesc();
}