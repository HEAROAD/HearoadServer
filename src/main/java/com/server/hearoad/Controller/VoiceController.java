package com.server.hearoad.Controller;

import com.server.hearoad.DTO.TTSResponse;
import com.server.hearoad.DTO.TTSFile;
import com.server.hearoad.Service.TTSService;
import com.server.hearoad.Repository.TTSFileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/voice")
public class VoiceController {

    private final TTSService ttsService;
    private final TTSFileRepository ttsFileRepository;

    public VoiceController(TTSService ttsService, TTSFileRepository ttsFileRepository) {
        this.ttsService = ttsService;
        this.ttsFileRepository = ttsFileRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("word") String word,
            @RequestParam("emoji") String emoji
    ) {
        if (word == null || word.isEmpty()) {
            return ResponseEntity.badRequest().body("단어는 꼭 입력해야 합니다.");
        }

        String mp3FilePath = ttsService.synthesizeSpeechToFile(word, "/path/to/save");

        if (mp3FilePath == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("MP3 파일을 생성하는데 실패했습니다.");
        }

        // MongoDB에 저장
        TTSFile ttsFile = new TTSFile();
        ttsFile.setWord(word);
        ttsFile.setEmoji(emoji);
        ttsFile.setFilePath(mp3FilePath);
        ttsFileRepository.save(ttsFile);

        // JSON 응답 반환
        TTSResponse response = new TTSResponse("MP3 파일이 생성되었습니다.", mp3FilePath, emoji);
        return ResponseEntity.ok(response);
    }
}
