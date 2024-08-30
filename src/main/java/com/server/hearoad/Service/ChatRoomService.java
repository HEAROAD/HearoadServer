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
}
