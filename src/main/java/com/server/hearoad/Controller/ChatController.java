package com.server.hearoad.Controller;

import com.server.hearoad.Model.ChatRoom;
import com.server.hearoad.Model.Message;
import com.server.hearoad.Service.ChatRoomService;
import com.server.hearoad.Service.KakaoService;
import com.server.hearoad.Service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final MessageService messageService;
    private final KakaoService kakaoService;

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
            @RequestPart(value = "message", required = false) String message,
            @RequestPart(value = "type") String type,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestHeader("chatRoomId") String chatRoomId,
            HttpServletRequest request) {

        String userId = kakaoService.getUserIdFromToken(request.getHeader("Authorization").substring(7));
        String fileUrl = null;
        String fileData = null;
        String fileName = null;

        // 조건에 따른 처리
        if ("USER".equals(type)) {
            // type이 USER일 때, MP4 파일이 필요
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(null); // 파일이 없을 경우 오류 처리
            }
            try {
                // 파일을 Base64 인코딩하여 저장
                fileData = Base64.getEncoder().encodeToString(file.getBytes());
                fileName = file.getOriginalFilename();
            } catch (IOException e) {
                return ResponseEntity.status(500).body(null); // 파일 처리 오류
            }
        } else if ("PARTNER".equals(type)) {
            // type이 PARTNER일 때, message가 필요
            if (message == null || message.isEmpty()) {
                return ResponseEntity.badRequest().body(null); // 메시지가 없을 경우 오류 처리
            }
        } else {
            return ResponseEntity.badRequest().body(null); // 지원하지 않는 type일 경우 오류 처리
        }

        // 메시지 전송
        Message sentMessage = messageService.sendMessage(chatRoomId, userId, type, message, fileUrl, fileData, fileName);
        return ResponseEntity.ok(sentMessage);
    }

    // 특정 채팅방의 메시지 조회
    @GetMapping("/messages/{chatRoomId}")
    public ResponseEntity<List<Message>> getMessages(@PathVariable String chatRoomId) {
        return ResponseEntity.ok(messageService.getMessagesByChatRoomId(chatRoomId));
    }
}
