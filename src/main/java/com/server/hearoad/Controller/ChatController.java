package com.server.hearoad.Controller;

import com.server.hearoad.Model.ChatMessage;
import com.server.hearoad.Model.ChatRoom;
import com.server.hearoad.Service.ChatRoomService;
import com.server.hearoad.Service.FileStorageService;
import com.server.hearoad.Service.KakaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final KakaoService kakaoService;
    private final FileStorageService fileStorageService;

    @PostMapping("/rooms")
    public ResponseEntity<ChatRoom> createChatRoom(@RequestHeader("Authorization") String authorizationHeader, @RequestParam String title) {
        String accessToken = extractToken(authorizationHeader);
        String nickname = kakaoService.getUserNicknameFromToken(accessToken);

        if (nickname == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        ChatRoom chatRoom = chatRoomService.createChatRoom(title, nickname);
        return new ResponseEntity<>(chatRoom, HttpStatus.CREATED);
    }

    @GetMapping("/rooms") //채팅방 리스트 반환
    public ResponseEntity<List<ChatRoom>> getAllChatRooms() {
        List<ChatRoom> chatRooms = chatRoomService.getAllChatRooms();
        return new ResponseEntity<>(chatRooms, HttpStatus.OK);
    }

    @GetMapping("/rooms/{roomId}") //채팅 반환
    public ResponseEntity<ChatRoom> getChatRoomById(@PathVariable String roomId) {
        return chatRoomService.getChatRoomById(roomId)
                .map(chatRoom -> new ResponseEntity<>(chatRoom, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // FastAPI 서버 URL을 설정하는 설정값 추가
    @Value("${fastapi.server.url}")
    private String fastApiServerUrl;

    @PostMapping("/rooms/{roomId}/messages")
    public ResponseEntity<Void> addMessageToChatRoom(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable String roomId,
            @RequestParam(value = "message", required = false) String message, // message를 선택적으로 설정
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "type", required = true) String type) {

        String accessToken = extractToken(authorizationHeader);
        String nickname = kakaoService.getUserNicknameFromToken(accessToken);

        if (nickname == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(type); // type 값을 저장
        chatMessage.setTimestamp(LocalDateTime.now());

        if (file != null && !file.isEmpty()) {
            if ("USER".equalsIgnoreCase(type)) {
                // 파일을 FastAPI 서버로 전송하여 예측 결과를 받음
                String predictedWord = sendFileToFastApiServer(file);
                chatMessage.setMessage(predictedWord); // 예측 결과로 메시지를 설정
            } else {
                // 파일 업로드 처리
                String imageUrl = fileStorageService.storeFile(file);
                chatMessage.setImageUrl(imageUrl);
            }
        } else if (message != null && !message.isEmpty()) {
            chatMessage.setMessage(message);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // message나 file이 없는 경우 400 Bad Request 반환
        }

        chatRoomService.addMessageToChatRoom(roomId, chatMessage);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // FastAPI 서버로 파일을 전송하고 응답을 받는 메소드
    private String sendFileToFastApiServer(MultipartFile file) {
        try {
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

            // 예측된 단어 반환
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return "Prediction Failed";
        }
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }
}