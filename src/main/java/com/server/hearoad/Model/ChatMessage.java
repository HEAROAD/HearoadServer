package com.server.hearoad.Model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ChatMessage {

    private String type; // "USER" or "PARTNER"
    private String message; // 텍스트 메시지
    private String imageUrl; // Image URL
    private LocalDateTime timestamp;
}
