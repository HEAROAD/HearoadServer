package com.server.hearoad.Controller;

import com.server.hearoad.Model.VoiceAnalysisResult;
import com.server.hearoad.Service.KakaoService;
import com.server.hearoad.Service.VoiceAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/analyze")
@RequiredArgsConstructor
public class VoiceAnalysisController {

    private final VoiceAnalysisService voiceAnalysisService;
    private final KakaoService kakaoService;

    @PostMapping("/voice")
    public ResponseEntity<?> analyzeVoice(@RequestParam("file") MultipartFile file, @RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (!authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token format");
            }
            String accessToken = authorizationHeader.substring(7);

            String nickname = kakaoService.getUserNicknameFromToken(accessToken);
            if (nickname == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token or user not found");
            }

            VoiceAnalysisResult result = voiceAnalysisService.analyzeVoice(file, nickname);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing file: " + e.getMessage());
        }
    }
}