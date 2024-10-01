package com.server.hearoad.Controller;

import com.server.hearoad.Model.ChatRoom;
import com.server.hearoad.Model.Message;
import com.server.hearoad.Service.ChatRoomService;
import com.server.hearoad.Service.KakaoService;
import com.server.hearoad.Service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final MessageService messageService; // 메시지 서비스 주입
    private final KakaoService kakaoService;

    // FastAPI 서버 URL 설정
    private final String fastApiServerUrl = "http://localhost:8000"; // FastAPI 서버 주소 설정

    // 채팅방 생성
    @PostMapping("/room")
    public ResponseEntity<ChatRoom> createChatRoom(@RequestBody ChatRoom chatRoom, HttpServletRequest request) {
        String userId = kakaoService.getUserIdFromToken(request.getHeader("Authorization").substring(7));
        ChatRoom createdChatRoom = chatRoomService.createChatRoom(userId, chatRoom.getTitle());
        return ResponseEntity.ok(createdChatRoom);
    }

    // 채팅방 목록 조회
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoom>> getChatRooms(HttpServletRequest request) {
        String userId = kakaoService.getUserIdFromToken(request.getHeader("Authorization").substring(7));
        return ResponseEntity.ok(chatRoomService.getChatRoomsByUserId(userId));
    }

    // 메시지 전송 (채팅방 ID를 헤더로 받음)
    @PostMapping("/message")
    public ResponseEntity<Message> sendMessage(
            @RequestPart("type") String type,
            @RequestPart("message") String message,
            @RequestHeader("chatRoomId") String chatRoomId,
            HttpServletRequest request) {

        String userId = kakaoService.getUserIdFromToken(request.getHeader("Authorization").substring(7));

        // 메시지 전송
        Message sentMessage = messageService.sendMessage(chatRoomId, userId, type, message, null, null, null);
        return ResponseEntity.ok(sentMessage);
    }

    // MP4 파일 전송 API
    @PostMapping("/file")
    public ResponseEntity<String> sendFileToFastApi(
            @RequestPart("file") MultipartFile file) {

        // 파일 확장자 체크
        String fileExtension = getFileExtension(file.getOriginalFilename());
        if (!isVideoFile(fileExtension)) {
            return ResponseEntity.status(415).body("Invalid file type. Only MP4 files are allowed.");
        }

        try {
            // FastAPI 서버로 파일 전송
            String response = sendFileToFastApiServer(file);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to upload file");
        }
    }

    // 특정 채팅방의 메시지 조회
    @GetMapping("/messages/{chatRoomId}")
    public ResponseEntity<List<Message>> getMessages(@PathVariable String chatRoomId) {
        return ResponseEntity.ok(messageService.getMessagesByChatRoomId(chatRoomId));
    }

    // FastAPI 서버로 파일을 전송하고 응답을 받는 메서드
    private String sendFileToFastApiServer(MultipartFile file) throws IOException {
        RestTemplate restTemplate = new RestTemplate();

        // 파일을 MultiValueMap에 추가
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // FastAPI 서버로 POST 요청 보내기
        ResponseEntity<String> response = restTemplate.postForEntity(fastApiServerUrl + "/predict", requestEntity, String.class);

        // FastAPI 서버의 응답 반환
        return response.getBody();
    }

    // 파일 확장자 확인하는 메서드
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(); // 확장자를 소문자로 반환
    }

    // 파일이 영상 파일인지 확인하는 메서드
    private boolean isVideoFile(String fileExtension) {
        return fileExtension.equals("mp4") || fileExtension.equals("avi") || fileExtension.equals("mov") || fileExtension.equals("mkv");
    }
}
