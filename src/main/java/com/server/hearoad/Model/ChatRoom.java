package com.server.hearoad.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chatrooms")
public class ChatRoom {
    @Id
    private String id;
    private String userId;
    private String title;
    private Date lastMessageTime;

    public ChatRoom(String userId, String title) {
        this.userId = userId;
        this.title = title;
        this.lastMessageTime = new Date();
    }
}
