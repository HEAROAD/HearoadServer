package com.server.hearoad.Service;

import com.server.hearoad.Model.ChatMessage;
import com.server.hearoad.Model.ChatRoom;
import com.server.hearoad.Repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;

    // 채팅방 생성
    public ChatRoom createChatRoom(String userId) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setUserId(userId);
        chatRoom.setCreatedTime(LocalDateTime.now());
        chatRoom.setMessages(List.of());

        return chatRoomRepository.save(chatRoom);
    }

    // 채팅 메시지 추가
    public ChatRoom addMessageToChatRoom(String chatRoomId, String sender, String message) {
        Optional<ChatRoom> chatRoomOptional = chatRoomRepository.findById(chatRoomId);

        if (chatRoomOptional.isPresent()) {
            ChatRoom chatRoom = chatRoomOptional.get();
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSender(sender);
            chatMessage.setMessage(message);
            chatMessage.setSentTime(LocalDateTime.now());

            chatRoom.getMessages().add(chatMessage);
            return chatRoomRepository.save(chatRoom);
        } else {
            throw new RuntimeException("ChatRoom not found");
        }
    }

    // 유저의 채팅방 목록 조회
    public List<ChatRoom> getUserChatRooms(String userId) {
        return chatRoomRepository.findByUserId(userId);
    }
}
