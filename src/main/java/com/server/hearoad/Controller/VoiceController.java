package com.server.hearoad.Controller;

import com.server.hearoad.DTO.TTSFile;
import com.server.hearoad.Repository.TTSFileRepository;
import com.server.hearoad.Service.TTSService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
    public ResponseEntity<String> uploadFile(
            @RequestParam("word") String word,
            @RequestParam("emoji") String emoji
    ) {
        if (word == null || word.isEmpty()) {
            return ResponseEntity.badRequest().body("단어는 꼭 입력해야 합니다.");
        }

        String mp3FilePath = ttsService.synthesizeSpeechToFile(word, "/path/to/save");

        if (mp3FilePath == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("MP3파일을 생성하는데 실패했습니다.");
        }
        TTSFile ttsFile = new TTSFile();
        ttsFile.setWord(word);
        ttsFile.setEmoji(emoji);
        ttsFile.setFilePath(mp3FilePath);
        ttsFileRepository.save(ttsFile);

        return ResponseEntity.ok("MP3 파일이 생성되었습니다: " + mp3FilePath + " | 이모지: " + emoji);
    }

}