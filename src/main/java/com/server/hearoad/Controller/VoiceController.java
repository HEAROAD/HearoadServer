package com.server.hearoad.Controller;

import com.server.hearoad.DTO.TTSResponse;
import com.server.hearoad.DTO.TTSFile;
import com.server.hearoad.Service.TTSService;
import com.server.hearoad.Repository.TTSFileRepository;
import com.server.hearoad.security.JwtTokenUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/voice")
public class VoiceController {

    private final TTSService ttsService;
    private final TTSFileRepository ttsFileRepository;
    private final JwtTokenUtil jwtTokenUtil;

    public VoiceController(TTSService ttsService, TTSFileRepository ttsFileRepository, JwtTokenUtil jwtTokenUtil) {
        this.ttsService = ttsService;
        this.ttsFileRepository = ttsFileRepository;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("word") String word,
            @RequestParam("emoji") String emoji,
            @RequestHeader("Authorization") String token // JWT 토큰을 요청 헤더에서 받습니다.
    ) {
        if (word == null || word.isEmpty()) {
            return ResponseEntity.badRequest().body("단어는 필수 입력사항입니다.");
        }

        String jwtToken = token.substring(7);

        String userId = jwtTokenUtil.getUsernameFromToken(jwtToken);

        String publicUrl = ttsService.synthesizeSpeechToFileAndUpload(word, emoji, userId);

        if (publicUrl == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Mp3파일을 생성하는 데 실패했습니다.");
        }

        TTSFile ttsFile = new TTSFile();
        ttsFile.setWord(word);
        ttsFile.setEmoji(emoji);
        ttsFile.setFilePath(publicUrl);
        ttsFile.setUserId(userId);
        ttsFileRepository.save(ttsFile);

        TTSResponse response = new TTSResponse("MP3 파일이 생성되었습니다.", publicUrl, emoji, word);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/files")
    public ResponseEntity<?> getUserFiles(@RequestHeader("Authorization") String token) {
        String jwtToken = token.substring(7);

        String userId = jwtTokenUtil.getUsernameFromToken(jwtToken);

        List<TTSFile> userFiles = ttsFileRepository.findByUserId(userId);

        if (userFiles.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 사용자의 음성 파일이 존재하지 않습니다.");
        }

        return ResponseEntity.ok(userFiles);
    }
}
