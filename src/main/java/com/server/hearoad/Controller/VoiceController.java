package com.server.hearoad.Controller;

import com.server.hearoad.DTO.TTSResponse;
import com.server.hearoad.DTO.TTSFile;
import com.server.hearoad.Repository.UserRepository;
import com.server.hearoad.Service.TTSService;
import com.server.hearoad.Service.KakaoService;
import com.server.hearoad.Repository.TTSFileRepository;
import com.server.hearoad.Model.User;
import com.server.hearoad.DTO.KakaoUserProfileDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/voice")
@RequiredArgsConstructor
public class VoiceController {

    private final TTSService ttsService;
    private final TTSFileRepository ttsFileRepository;
    private final KakaoService kakaoService;
    private final UserRepository userRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("word") String word,
            @RequestParam("emoji") String emoji,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String accessToken  // 카카오 Access Token을 헤더로 받음
    ) {
        // Bearer 토큰에서 실제 Access Token만 추출
        String token = accessToken.replace("Bearer ", "");

        // Access Token을 사용하여 카카오에서 사용자 프로필을 가져옴
        KakaoUserProfileDto kakaoUserProfile = kakaoService.getUserProfile(token);

        if (kakaoUserProfile == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("사용자 정보를 가져올 수 없습니다.");
        }

        // 사용자 정보 저장 또는 업데이트
        User user = kakaoService.saveOrUpdateUser(kakaoUserProfile);

        // TTS 파일 생성 및 업로드
        String publicUrl = ttsService.synthesizeSpeechToFileAndUpload(word, emoji);

        if (publicUrl == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Mp3 파일을 생성하는 데 실패했습니다.");
        }

        // TTSFile 객체 생성 및 저장
        TTSFile ttsFile = new TTSFile();
        ttsFile.setWord(word);
        ttsFile.setEmoji(emoji);
        ttsFile.setFilePath(publicUrl);
        ttsFile.setUser(user);
        ttsFileRepository.save(ttsFile);

        // 응답 생성
        TTSResponse response = new TTSResponse("MP3 파일이 생성되었습니다.", publicUrl, emoji, word);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/files")
    public ResponseEntity<List<TTSFile>> getUserFiles(@RequestHeader("Authorization") String accessToken) {
        String token = accessToken.replace("Bearer ", "");

        String kakaoUserId = String.valueOf(kakaoService.getUserProfile(token).getId());

        User user = userRepository.findById(kakaoUserId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        List<TTSFile> userFiles = ttsFileRepository.findByUser(user);

        return ResponseEntity.ok(userFiles);
    }

    @DeleteMapping("/delete/{fileId}")
    public ResponseEntity<?> deleteUserFile(
            @PathVariable String fileId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String accessToken
    ) {
        String token = accessToken.replace("Bearer ", "");

        String kakaoUserId = String.valueOf(kakaoService.getUserProfile(token).getId());

        User user = userRepository.findById(kakaoUserId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        TTSFile ttsFile = ttsFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다."));

        if (!ttsFile.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("해당 파일을 삭제할 권한이 없습니다.");
        }

        boolean isDeletedFromS3 = ttsService.deleteFileFromS3(ttsFile.getFilePath());
        if (!isDeletedFromS3) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("S3에서 파일 삭제에 실패했습니다.");
        }

        // 데이터베이스에서 파일 삭제
        ttsFileRepository.delete(ttsFile);

        return ResponseEntity.ok("파일이 성공적으로 삭제되었습니다.");
    }

}