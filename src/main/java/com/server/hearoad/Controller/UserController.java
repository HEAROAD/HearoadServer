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

    @GetMapping("/mypage")
    public ResponseEntity<UserProfileResponse> getUserInfo(HttpServletRequest request) {
        try {
            String accessToken = request.getHeader("Authorization").substring(7);
            String userId = kakaoService.getUserIdFromToken(accessToken);

            User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found"));

            List<VoiceAnalysisResult> results = voiceAnalysisResultRepository.findByNickname(user.getNickname());

            String character = "히로";

            if (!results.isEmpty()) {
                String recentCharacterJson = results.get(results.size() - 1).getAnalysisResult();
                if (recentCharacterJson != null && !recentCharacterJson.isEmpty()) {
                    character = extractCharacterFromJson(recentCharacterJson);
                }
            }

            UserProfileResponse response = new UserProfileResponse(user.getNickname(), character);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
    }

    private String extractCharacterFromJson(String json) {
        if (json == null || json.isEmpty()) {
            return "히로";
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> responseMap = objectMapper.readValue(json, Map.class);
            return responseMap.getOrDefault("character", "히로").toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "히로";
        }
    }

    @PostMapping("/analyze/voice")
    public ResponseEntity<?> analyzeVoice(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        try {
            String accessToken = request.getHeader("Authorization").substring(7);
            String userId = kakaoService.getUserIdFromToken(accessToken);

            User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found"));

            VoiceAnalysisResult result = voiceAnalysisService.analyzeVoice(file, user.getNickname());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}