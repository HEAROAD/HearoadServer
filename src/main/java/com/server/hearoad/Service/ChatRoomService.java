package com.server.hearoad.Service;

import com.server.hearoad.Model.ChatRoom;
import com.server.hearoad.Repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;

    public ChatRoom createChatRoom(String userId, String title) {
        ChatRoom chatRoom = new ChatRoom(userId, title);
        return chatRoomRepository.save(chatRoom);
    }

    public List<ChatRoom> getChatRoomsByUserId(String userId) {
        return chatRoomRepository.findByUserId(userId);
    }

    public void updateLastMessageTime(String chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new NoSuchElementException("ChatRoom not found"));
        chatRoom.setLastMessageTime(new Date());
        chatRoomRepository.save(chatRoom);
    }
}
