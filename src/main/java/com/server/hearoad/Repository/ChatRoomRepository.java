package com.server.hearoad.Repository;

import com.server.hearoad.Model.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {

    // Find chat rooms by title (existing method)
    Optional<ChatRoom> findByTitle(String title);

    }
