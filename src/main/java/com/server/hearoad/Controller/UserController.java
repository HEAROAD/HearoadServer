package com.server.hearoad.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.hearoad.DTO.UserProfileResponse;
import com.server.hearoad.Model.User;
import com.server.hearoad.Model.VoiceAnalysisResult;
import com.server.hearoad.Repository.UserRepository;
import com.server.hearoad.Repository.VoiceAnalysisResultRepository;
import com.server.hearoad.Service.KakaoService;
import com.server.hearoad.Service.VoiceAnalysisService;
import com.server.hearoad.Response.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final KakaoService kakaoService;
    private final UserRepository userRepository;
    private final VoiceAnalysisResultRepository voiceAnalysisResultRepository;
    private final VoiceAnalysisService voiceAnalysisService;

    @PostMapping("/login/oauth/kakao")
    public ResponseEntity<LoginResponse> kakaoLoginFromAndroid(@RequestParam String token) {
        try {
            return ResponseEntity.ok(kakaoService.kakaoLoginWithToken(token));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 사용자 정보 조회 엔드포인트
    @GetMapping("/mypage")
    public ResponseEntity<UserProfileResponse> getUserInfo(HttpServletRequest request) {
        try {
            // JWT 토큰에서 사용자 ID 추출
            String accessToken = request.getHeader("Authorization").substring(7); // "Bearer " 제거
            String userId = kakaoService.getUserIdFromToken(accessToken);

            // 사용자 정보 조회
            User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found"));

            // 최근 음성 분석 결과 조회
            List<VoiceAnalysisResult> results = voiceAnalysisResultRepository.findByNickname(user.getNickname());

            // 기본 캐릭터 값 설정
            String character = "히로";

            if (!results.isEmpty()) {
                // 최신 결과의 analysisResult 값이 null이 아닌지 확인
                String recentCharacterJson = results.get(results.size() - 1).getAnalysisResult();
                if (recentCharacterJson != null && !recentCharacterJson.isEmpty()) {
                    character = extractCharacterFromJson(recentCharacterJson);
                }
            }

            // 사용자 프로필 생성
            UserProfileResponse response = new UserProfileResponse(user.getNickname(), character);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
    }

    // JSON 문자열에서 character 필드 추출 메서드
    private String extractCharacterFromJson(String json) {
        if (json == null || json.isEmpty()) {
            return "히로"; // json이 null이거나 빈 문자열일 경우 기본값 "히로" 반환
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> responseMap = objectMapper.readValue(json, Map.class);
            return responseMap.getOrDefault("character", "히로").toString(); // 캐릭터가 없으면 기본값 "히로" 반환
        } catch (Exception e) {
            e.printStackTrace();
            return "히로"; // JSON 파싱 실패 시 기본값 "히로" 반환
        }
    }

    // 음성 파일 업로드 및 분석 엔드포인트
    @PostMapping("/analyze/voice")
    public ResponseEntity<?> analyzeVoice(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        try {
            // JWT 토큰에서 사용자 ID 추출
            String accessToken = request.getHeader("Authorization").substring(7); // "Bearer " 제거
            String userId = kakaoService.getUserIdFromToken(accessToken);

            // 사용자 정보 조회
            User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found"));

            // 음성 파일 분석
            VoiceAnalysisResult result = voiceAnalysisService.analyzeVoice(file, user.getNickname());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}