package com.server.hearoad.Repository;

import com.server.hearoad.Model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findByChatRoomId(String chatRoomId);
}