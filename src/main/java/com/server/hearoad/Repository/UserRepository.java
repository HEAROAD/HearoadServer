package com.server.hearoad.Repository;

import com.server.hearoad.Model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByNickname(String nickname);
    Optional<User> findById(String id);
}
