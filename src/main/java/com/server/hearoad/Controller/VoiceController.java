package com.server.hearoad.Controller;

import com.server.hearoad.DTO.TTSResponse;
import com.server.hearoad.DTO.TTSFile;
import com.server.hearoad.Service.TTSService;
import com.server.hearoad.Repository.TTSFileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            return ResponseEntity.badRequest().body("단어는 필수 입력사항입니다.");
        }

        String publicUrl = ttsService.synthesizeSpeechToFileAndUpload(word, emoji);

        if (publicUrl == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Mp3파일을 생성하는 데 실패했습니다.");
        }

        TTSFile ttsFile = new TTSFile();
        ttsFile.setWord(word);
        ttsFile.setEmoji(emoji);
        ttsFile.setFilePath(publicUrl);
        // Remove setting userId
        // ttsFile.setUserId(userId);
        ttsFileRepository.save(ttsFile);

        TTSResponse response = new TTSResponse("MP3 파일이 생성되었습니다.", publicUrl, emoji, word);
        return ResponseEntity.ok(response);
    }

//    @GetMapping("/files")
//    public ResponseEntity<?> getUserFiles(@RequestParam("userId") String userId) {
//        List<TTSFile> userFiles = ttsFileRepository.findByUserId(userId);
//
//        if (userFiles.isEmpty()) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 사용자의 음성 파일이 존재하지 않습니다.");
//        }
//
//        return ResponseEntity.ok(userFiles);
//    }
}
