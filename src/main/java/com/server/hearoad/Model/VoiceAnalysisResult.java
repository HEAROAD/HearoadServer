package com.server.hearoad.Model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "voice_analysis_results")
public class VoiceAnalysisResult {

    @Id
    private String id;
    private String nickname;
    private String character;

    public VoiceAnalysisResult(String nickname, String character) {
        this.nickname = nickname;
        this.character = character;
    }
}
