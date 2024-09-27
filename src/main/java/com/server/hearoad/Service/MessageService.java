package com.server.hearoad.Service;

import com.server.hearoad.Model.Message;
import com.server.hearoad.Repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final ChatRoomService chatRoomService;

    public Message sendMessage(String chatRoomId, String userId, String type, String message, String fileUrl, String fileData, String fileName) {
        Message msg = new Message(chatRoomId, userId, type, message, fileUrl, fileData, fileName);
        chatRoomService.updateLastMessageTime(chatRoomId); // 메시지 전송 시 채팅방 마지막 메시지 시간 업데이트
        return messageRepository.save(msg);
    }

    public List<Message> getMessagesByChatRoomId(String chatRoomId) {
        return messageRepository.findByChatRoomId(chatRoomId);
    }
}