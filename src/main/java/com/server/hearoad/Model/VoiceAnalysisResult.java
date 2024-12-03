package com.server.hearoad.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "voice_analysis_results")
public class VoiceAnalysisResult {
    @Id
    private String id;
    private String nickname;
    private String analysisResult;
    private String character;

    public VoiceAnalysisResult(String nickname, String analysisResult, String character) {
        this.nickname = nickname;
        this.analysisResult = analysisResult;
        this.character = character;
    }
}
