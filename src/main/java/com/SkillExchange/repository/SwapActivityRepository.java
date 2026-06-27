package com.SkillExchange.repository;

import com.SkillExchange.model.SwapActivity;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface SwapActivityRepository extends MongoRepository<SwapActivity, String> {
    // Fetch latest activities sorted by timestamp descending
    List<SwapActivity> findTop10ByOrderByTimestampDesc();
}