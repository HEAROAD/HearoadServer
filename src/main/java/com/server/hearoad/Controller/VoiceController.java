package com.server.hearoad.Controller;

import com.server.hearoad.Service.TTSService;
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

    public VoiceController() {
        this.ttsService = new TTSService();
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("word") String word,
            @RequestParam("image") MultipartFile image) {

        String mp3FilePath = ttsService.synthesizeSpeechToFile(word, "output.mp3"); //여기 filename으로 수정

        return ResponseEntity.ok("File uploaded and TTS created at: " + mp3FilePath);
    }
}