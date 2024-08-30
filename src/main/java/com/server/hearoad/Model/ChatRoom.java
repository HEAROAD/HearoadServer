package com.server.hearoad.Model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "chatrooms")
public class ChatRoom {
    @Id
    private String id;

    private String title;

    private String creatorNickname;

    private LocalDateTime lastMessageTime;

    private List<ChatMessage> messages = new ArrayList<>(); // 초기화 추가

    public ChatRoom(String title, String creatorNickname) {
        this.title = title;
        this.creatorNickname = creatorNickname;
        this.lastMessageTime = LocalDateTime.now();
        this.messages = new ArrayList<>(); // 초기화 추가
    }
}
