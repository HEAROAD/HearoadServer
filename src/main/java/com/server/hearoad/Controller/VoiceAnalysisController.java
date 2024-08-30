package com.server.hearoad.Controller;

import com.server.hearoad.DTO.VoiceAnalysisResponse;
import com.server.hearoad.Model.VoiceAnalysisResult;
import com.server.hearoad.Repository.VoiceAnalysisResultRepository;
import com.server.hearoad.Service.KakaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.file.*;

@RestController
@RequestMapping("/analyze")
public class VoiceAnalysisController {

    @Autowired
    private KakaoService kakaoService;

    @Autowired
    private VoiceAnalysisResultRepository voiceAnalysisResultRepository;

    @PostMapping("/voice")
    public ResponseEntity<?> analyzeVoice(@RequestParam("file") MultipartFile file,
                                          @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Authorization 헤더에서 Bearer 토큰 추출
            if (!authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("잘못된 인증 형식입니다.");
            }
            String accessToken = authorizationHeader.substring(7);

            // KakaoService를 통해 사용자 닉네임 가져오기
            String nickname = kakaoService.getUserNicknameFromToken(accessToken);
            if (nickname == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("잘못된 액세스 토큰입니다.");
            }

            // 파일을 임시 디렉토리에 저장
            Path tempFile = Files.createTempFile("voice_", ".mp3");
            file.transferTo(tempFile.toFile());

            // Python 스크립트 실행
            ProcessBuilder pb = new ProcessBuilder("python", "scripts/analyze_voice.py", tempFile.toString());
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("음성 파일 처리 중 오류가 발생했습니다.");
            }

            // 결과를 JSON으로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            VoiceAnalysisResponse response = objectMapper.readValue(result.toString(), VoiceAnalysisResponse.class);

            // 캐릭터와 닉네임을 데이터베이스에 저장
            VoiceAnalysisResult analysisResult = new VoiceAnalysisResult(nickname, response.getCharacter());
            voiceAnalysisResultRepository.save(analysisResult);

            // 응답 반환
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("음성 분석 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
