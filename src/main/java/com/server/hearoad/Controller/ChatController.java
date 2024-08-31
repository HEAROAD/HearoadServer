package com.server.hearoad.Controller;

import com.server.hearoad.Model.ChatMessage;
import com.server.hearoad.Model.ChatRoom;
import com.server.hearoad.Service.ChatRoomService;
import com.server.hearoad.Service.FileStorageService;
import com.server.hearoad.Service.KakaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping("/rooms/{roomId}/messages") // 메세지 보내기
    public ResponseEntity<Void> addMessageToChatRoom(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable String roomId,
            @RequestParam("message") String message,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        String accessToken = extractToken(authorizationHeader);
        String nickname = kakaoService.getUserNicknameFromToken(accessToken);

        if (nickname == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMessage(message);
        chatMessage.setType("USER"); // 또는 "PARTNER" - 로직에 따라 구분
        chatMessage.setTimestamp(LocalDateTime.now());

        if (file != null && !file.isEmpty()) {
            // 파일 업로드 처리
            String imageUrl = fileStorageService.storeFile(file); // 파일을 저장하고 URL을 반환하는 서비스 구현 필요
            chatMessage.setImageUrl(imageUrl);
        }

        chatRoomService.addMessageToChatRoom(roomId, chatMessage);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }
}
