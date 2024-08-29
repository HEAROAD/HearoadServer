package com.server.hearoad.Repository;
import com.server.hearoad.DTO.TTSFile;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TTSFileRepository extends MongoRepository<TTSFile, String> {
}
