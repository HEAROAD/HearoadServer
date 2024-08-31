package com.server.hearoad.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/analyze")
public class VoiceAnalysisController {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @PostMapping("/voice")
    public ResponseEntity<?> analyzeVoice(@RequestParam("file") MultipartFile file) {
        try {
            // 업로드된 파일을 임시 디렉토리에 저장
            Path tempFile = Files.createTempFile("voice_", ".m4a");
            file.transferTo(tempFile.toFile());

            // FFmpeg를 사용하여 m4a 파일을 mp3로 변환
            Path mp3File = Files.createTempFile("voice_", ".mp3");
            ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-i", tempFile.toString(), mp3File.toString());
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // FFmpeg 프로세스 출력 로그 확인
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("FFmpeg 처리 중 오류가 발생했습니다.");
            }

            // FastAPI로 변환된 mp3 파일 전송
            String fastApiResponse = sendFileToFastApi(mp3File);
            return ResponseEntity.ok(fastApiResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private String sendFileToFastApi(Path mp3File) {
        WebClient webClient = webClientBuilder.build();

        FileSystemResource resource = new FileSystemResource(mp3File.toFile());

        return webClient.post()
                .uri("http://your-backend-url.com/analyze")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(resource)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}

