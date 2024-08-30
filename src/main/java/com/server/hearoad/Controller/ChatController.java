package com.server.hearoad.Controller;

import com.server.hearoad.Model.ChatRoom;
import com.server.hearoad.Service.ChatService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // 채팅방 생성
    @PostMapping("/create")
    public ResponseEntity<ChatRoom> createChatRoom(HttpSession session) {
        String userId = (String) session.getAttribute("userId");

        if (userId == null) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }

        ChatRoom chatRoom = chatService.createChatRoom(userId);
        return new ResponseEntity<>(chatRoom, HttpStatus.CREATED);
    }

    // 채팅 메시지 추가
    @PostMapping("/{chatRoomId}/message")
    public ResponseEntity<ChatRoom> sendMessage(@PathVariable String chatRoomId,
                                                @RequestParam String message,
                                                HttpSession session) {
        String userId = (String) session.getAttribute("userId");

        if (userId == null) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }

        ChatRoom chatRoom = chatService.addMessageToChatRoom(chatRoomId, userId, message);
        return new ResponseEntity<>(chatRoom, HttpStatus.OK);
    }

    // 채팅방 목록 조회
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoom>> getUserChatRooms(HttpSession session) {
        String userId = (String) session.getAttribute("userId");

        if (userId == null) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }

        List<ChatRoom> chatRooms = chatService.getUserChatRooms(userId);
        return new ResponseEntity<>(chatRooms, HttpStatus.OK);
    }
}
