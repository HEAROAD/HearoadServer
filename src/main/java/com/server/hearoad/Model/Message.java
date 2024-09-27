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
    private String fileUrl; // 외부 파일 URL (Base64 저장하지 않을 때 사용)
    private String fileData; // Base64 인코딩된 파일 데이터 (MongoDB에 저장)
    private String fileName; // 파일 이름
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
