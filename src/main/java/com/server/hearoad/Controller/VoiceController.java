package com.server.hearoad.Controller;

import com.server.hearoad.DTO.TTSResponse;
import com.server.hearoad.DTO.TTSFile;
import com.server.hearoad.Repository.UserRepository;
import com.server.hearoad.Service.TTSService;
import com.server.hearoad.Service.KakaoService;
import com.server.hearoad.Repository.TTSFileRepository;
import com.server.hearoad.Model.User;
import com.server.hearoad.Tokens.Generator.JwtTokenProvider;
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
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("word") String word,
            @RequestParam("emoji") String emoji,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String jwtToken
    ) {
        String token = jwtToken.replace("Bearer ", "");
        String userId = jwtTokenProvider.getSubject(token);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));


        String publicUrl = ttsService.synthesizeSpeechToFileAndUpload(word, emoji);

        if (publicUrl == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Mp3 파일을 생성하는 데 실패했습니다.");
        }

        TTSFile ttsFile = new TTSFile();
        ttsFile.setWord(word);
        ttsFile.setEmoji(emoji);
        ttsFile.setFilePath(publicUrl);
        ttsFile.setUser(user);
        ttsFileRepository.save(ttsFile);

        TTSResponse response = new TTSResponse("MP3 파일이 생성되었습니다.", publicUrl, emoji, word);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/files")
    public ResponseEntity<List<TTSFile>> getUserFiles(@RequestHeader("Authorization") String jwtToken) {
        String token = jwtToken.replace("Bearer ", "");
        String userId = jwtTokenProvider.getSubject(token);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        List<TTSFile> userFiles = ttsFileRepository.findByUser(user);

        return ResponseEntity.ok(userFiles);
    }

    @DeleteMapping("/delete/{fileId}")
    public ResponseEntity<?> deleteUserFile(
            @PathVariable String fileId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String jwtToken
    ) {
        String token = jwtToken.replace("Bearer ", "");
        String userId = jwtTokenProvider.getSubject(token);

        User user = userRepository.findById(userId)
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

        ttsFileRepository.delete(ttsFile);

        return ResponseEntity.ok("파일이 성공적으로 삭제되었습니다.");
    }
}
