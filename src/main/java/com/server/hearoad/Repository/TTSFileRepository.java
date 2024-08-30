package com.server.hearoad.Repository;

import com.server.hearoad.DTO.TTSFile;
import com.server.hearoad.Model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface TTSFileRepository extends MongoRepository<TTSFile, String> {
    List<TTSFile> findAllByUser(User user);
}