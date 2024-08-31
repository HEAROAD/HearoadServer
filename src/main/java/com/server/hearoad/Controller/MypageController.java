package com.server.hearoad.Controller;

import com.server.hearoad.DTO.VoiceAnalysisResponse;
import com.server.hearoad.Model.VoiceAnalysisResult;
import com.server.hearoad.Repository.VoiceAnalysisResultRepository;
import com.server.hearoad.Service.KakaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

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
        Optional<VoiceAnalysisResult> optionalResult = voiceAnalysisResultRepository.findByNickname(nickname);
        String character = optionalResult.map(VoiceAnalysisResult::getCharacter).orElse("히로");

        // 응답 객체 생성
        VoiceAnalysisResponse response = new VoiceAnalysisResponse();
        response.setNickname(nickname);
        response.setCharacter(character);

        // 응답 반환
        return ResponseEntity.ok(response);
    }
}
