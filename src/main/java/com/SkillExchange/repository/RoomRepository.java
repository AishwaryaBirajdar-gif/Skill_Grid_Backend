package com.SkillExchange.repository;

import com.SkillExchange.model.Room;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends MongoRepository<Room, String> {
    // Find room by the custom roomId/requestId
    Room findByRoomId(String roomId);
}