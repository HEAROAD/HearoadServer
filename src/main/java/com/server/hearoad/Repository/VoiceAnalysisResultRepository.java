package com.server.hearoad.Repository;

import com.server.hearoad.Model.VoiceAnalysisResult;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface VoiceAnalysisResultRepository extends MongoRepository<VoiceAnalysisResult, String> {
    List<VoiceAnalysisResult> findByNickname(String nickname);
}
