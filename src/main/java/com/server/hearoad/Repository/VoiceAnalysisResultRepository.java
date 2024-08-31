package com.server.hearoad.Repository;

import com.server.hearoad.Model.VoiceAnalysisResult;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface VoiceAnalysisResultRepository extends MongoRepository<VoiceAnalysisResult, String> {
    Optional<VoiceAnalysisResult> findByNickname(String nickname);
}

