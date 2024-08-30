package com.server.hearoad.Model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "chatrooms")
@Getter
@Setter
public class ChatRoom {
    @Id
    private String id;
    private String userId; // 유저 ID
    private LocalDateTime createdTime; // 생성 시간
    private List<ChatMessage> messages; // 채팅 메시지들
}
