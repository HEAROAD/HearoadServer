package com.server.hearoad.kakao;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, Long> {
    Optional<User> findById(Long id);
}
