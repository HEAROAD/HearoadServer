package com.server.hearoad.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.hearoad.DTO.VoiceAnalysisResponse;
import com.server.hearoad.Model.VoiceAnalysisResult;
import com.server.hearoad.Repository.VoiceAnalysisResultRepository;
import com.server.hearoad.Service.KakaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/users")
public class MypageController {

    @Autowired
    private KakaoService kakaoService;

    @Autowired
    private VoiceAnalysisResultRepository voiceAnalysisResultRepository;

    @GetMapping("/mypage")
    public ResponseEntity<VoiceAnalysisResponse> getUserProfile(@RequestHeader("Authorization") String authorizationHeader) {
        // Authorization 헤더에서 Bearer 토큰 추출
        if (!authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        String accessToken = authorizationHeader.substring(7);

        // 사용자 닉네임 가져오기
        String nickname = kakaoService.getUserNicknameFromToken(accessToken);
        if (nickname == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // 데이터베이스에서 음성 분석 결과 찾기
        List<VoiceAnalysisResult> results = voiceAnalysisResultRepository.findByNickname(nickname);

        // 가장 최신의 결과를 선택
        String character = "히로";
        if (!results.isEmpty()) {
            // JSON 응답에서 character 필드만 추출
            String fastApiResponse = results.get(results.size() - 1).getCharacter();
            character = extractCharacterFromJson(fastApiResponse);
        }

        // 응답 객체 생성
        VoiceAnalysisResponse response = new VoiceAnalysisResponse();
        response.setNickname(nickname);
        response.setCharacter(character);

        // 응답 반환
        return ResponseEntity.ok(response);
    }

    private String extractCharacterFromJson(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> responseMap = objectMapper.readValue(json, Map.class);
            return responseMap.get("character").toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "히로";
        }
    }
}
