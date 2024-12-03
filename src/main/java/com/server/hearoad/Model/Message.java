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
@Document(collection = "messages")
public class Message {
    @Id
    private String id;
    private String chatRoomId;
    private String userId;
    private String type;
    private String message;
    private String fileUrl;
    private String fileData;
    private String fileName;
    private Date timestamp;

    public Message(String chatRoomId, String userId, String type, String message, String fileUrl, String fileData, String fileName) {
        this.chatRoomId = chatRoomId;
        this.userId = userId;
        this.type = type;
        this.message = message;
        this.fileUrl = fileUrl;
        this.fileData = fileData;
        this.fileName = fileName;
        this.timestamp = new Date();
    }
}
