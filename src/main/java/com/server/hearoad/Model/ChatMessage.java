package com.server.hearoad.Model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChatMessage {
    private String sender;
    private String message;
    private LocalDateTime sentTime;
}
