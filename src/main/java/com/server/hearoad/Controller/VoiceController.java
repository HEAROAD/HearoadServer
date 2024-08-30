package com.server.hearoad.Controller;

import com.server.hearoad.DTO.TTSResponse;
import com.server.hearoad.DTO.TTSFile;
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

@RestController
@RequestMapping("/api/voice")
@RequiredArgsConstructor
public class VoiceController {

    private final TTSService ttsService;
    private final TTSFileRepository ttsFileRepository;
    private final KakaoService kakaoService;

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


// 사용자의 MP3 파일을 조회하는 API
//    @GetMapping("/files")
//    public ResponseEntity<List<TTSFile>> getUserFiles(@RequestParam("userId") String userId) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
//
//        List<TTSFile> userFiles = ttsFileRepository.findAllByUser(user);
//
//        if (userFiles.size() < 3) {
//            // 기본 파일 생성 및 저장 (여기서는 생략됨)
//            generateDefaultFilesForUser(user);
//            userFiles = ttsFileRepository.findAllByUser(user);
//        }
//
//        return ResponseEntity.ok(userFiles);
//    }
//
//    private void generateDefaultFilesForUser(User user) {
//        // 기본 TTS 파일을 생성하고 저장하는 로직을 구현하세요.
//    }

}