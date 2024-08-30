package com.server.hearoad.Service;

import com.server.hearoad.Model.ChatRoom;
import com.server.hearoad.Model.ChatMessage;
import com.server.hearoad.Repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    public ChatRoom createChatRoom(String title, String creatorNickname) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setTitle(title);
        chatRoom.setCreatorNickname(creatorNickname);
        chatRoom.setLastMessageTime(LocalDateTime.now());
        chatRoom.setMessages(List.of());

        return chatRoomRepository.save(chatRoom);
    }

    public Optional<ChatRoom> getChatRoomById(String id) {
        return chatRoomRepository.findById(id);
    }

    public List<ChatRoom> getAllChatRooms() {
        return chatRoomRepository.findAll();
    }

    public void addMessageToChatRoom(String roomId, ChatMessage chatMessage) {
        Optional<ChatRoom> chatRoomOpt = chatRoomRepository.findById(roomId);
        if (chatRoomOpt.isPresent()) {
            ChatRoom chatRoom = chatRoomOpt.get();
            chatRoom.getMessages().add(chatMessage);
            chatRoom.setLastMessageTime(LocalDateTime.now());
            chatRoomRepository.save(chatRoom);
        }
    }

    public ChatMessage processMessage(ChatMessage chatMessage) {
        if ("partner".equalsIgnoreCase(chatMessage.getType())) {
            // 텍스트를 수어로 변환하는 로직
            chatMessage.setKsl(convertTextToKSL(chatMessage.getMessage()));
            chatMessage.setText("번역된 글로스"); // 예시
        } else if ("user".equalsIgnoreCase(chatMessage.getType()) && chatMessage.getKsl() != null) {
            // 수어 영상을 텍스트로 변환하는 로직
            chatMessage.setMessage(convertKSLToText(chatMessage.getKsl()));
        }
        return chatMessage;
    }

    private String convertTextToKSL(String message) {
        // 텍스트를 수어로 변환하는 로직 (API 호출 또는 내부 로직)
        return "변환된 수어영상";
    }

    private String convertKSLToText(String ksl) {
        // 수어 영상을 텍스트로 변환하는 로직 (API 호출 또는 내부 로직)
        return "번역텍스트";
    }
}
