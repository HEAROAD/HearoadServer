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
    private String id; // MongoDB 문서 ID
    private String nickname; // 분석을 요청한 사용자 닉네임
    private String analysisResult; // 분석 결과 JSON
    private String character; // 분석 결과에서 추출한 캐릭터 정보

    public VoiceAnalysisResult(String nickname, String analysisResult, String character) {
        this.nickname = nickname;
        this.analysisResult = analysisResult;
        this.character = character;
    }
}
