package com.server.hearoad.Controller;

import com.server.hearoad.DTO.TTSResponse;
import com.server.hearoad.DTO.TTSFile;
import com.server.hearoad.Service.TTSService;
import com.server.hearoad.Repository.TTSFileRepository;
import com.server.hearoad.Repository.UserRepository;
import com.server.hearoad.Model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/voice")
public class VoiceController {

    private final TTSService ttsService;
    private final TTSFileRepository ttsFileRepository;
    private final UserRepository userRepository;  // UserRepository 주입

    public VoiceController(TTSService ttsService, TTSFileRepository ttsFileRepository, UserRepository userRepository) {
        this.ttsService = ttsService;
        this.ttsFileRepository = ttsFileRepository;
        this.userRepository = userRepository;  // UserRepository 초기화
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("word") String word,
            @RequestParam("emoji") String emoji,
            @RequestParam("userId") String userId  // 사용자 ID를 요청에서 받아옴
    ) {
        if (word == null || word.isEmpty()) {
            return ResponseEntity.badRequest().body("단어는 필수 입력사항입니다.");
        }

        String publicUrl = ttsService.synthesizeSpeechToFileAndUpload(word, emoji);

        if (publicUrl == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Mp3파일을 생성하는 데 실패했습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        TTSFile ttsFile = new TTSFile();
        ttsFile.setWord(word);
        ttsFile.setEmoji(emoji);
        ttsFile.setFilePath(publicUrl);
        ttsFile.setUser(user);  // 파일을 사용자와 연관시킴
        ttsFileRepository.save(ttsFile);

        TTSResponse response = new TTSResponse("MP3 파일이 생성되었습니다.", publicUrl, emoji, word);
        return ResponseEntity.ok(response);
    }

    // 사용자의 MP3 파일을 조회하는 API
    @GetMapping("/files")
    public ResponseEntity<List<TTSFile>> getUserFiles(@RequestParam("userId") String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        List<TTSFile> userFiles = ttsFileRepository.findAllByUser(user);

        // 사용자가 최소 3개의 파일을 가지고 있는지 확인
        if (userFiles.size() < 3) {
            // 기본 파일 생성 및 저장 (여기서는 생략됨)
            generateDefaultFilesForUser(user);
            userFiles = ttsFileRepository.findAllByUser(user);
        }

        return ResponseEntity.ok(userFiles);
    }

    // 기본 파일 생성 로직 (여기서는 생략됨)
    private void generateDefaultFilesForUser(User user) {
        // 기본 TTS 파일을 생성하고 저장하는 로직을 구현하세요.
    }
}
