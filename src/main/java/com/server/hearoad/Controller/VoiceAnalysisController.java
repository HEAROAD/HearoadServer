package com.server.hearoad.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.hearoad.Model.VoiceAnalysisResult;
import com.server.hearoad.Repository.VoiceAnalysisResultRepository;
import com.server.hearoad.Service.KakaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/analyze")
public class VoiceAnalysisController {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private KakaoService kakaoService;

    @Autowired
    private VoiceAnalysisResultRepository voiceAnalysisResultRepository;

    @PostMapping("/voice")
    public ResponseEntity<?> analyzeVoice(@RequestParam("file") MultipartFile file, @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Authorization 헤더에서 Bearer 토큰 추출
            if (!authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }
            String accessToken = authorizationHeader.substring(7);

            // 사용자 닉네임 가져오기
            String nickname = kakaoService.getUserNicknameFromToken(accessToken);
            if (nickname == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }

            // 업로드된 파일을 임시 디렉토리에 저장
            Path tempFile = Files.createTempFile("voice_", "." + getFileExtension(file.getOriginalFilename()));
            file.transferTo(tempFile.toFile());

            // FastAPI로 파일 전송
            String fastApiResponse = sendFileToFastApi(tempFile);

            // JSON 응답을 받아 VoiceAnalysisResult 객체로 저장
            VoiceAnalysisResult result = new VoiceAnalysisResult(nickname, fastApiResponse);

            // 결과를 데이터베이스에 저장
            voiceAnalysisResultRepository.save(result);

            return ResponseEntity.ok(fastApiResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private String sendFileToFastApi(Path filePath) {
        WebClient webClient = webClientBuilder.build();

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(filePath.toFile()));

        return webClient.post()
                .uri("http://localhost:8000/analyze-voice/")  // FastAPI 서버 주소
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
