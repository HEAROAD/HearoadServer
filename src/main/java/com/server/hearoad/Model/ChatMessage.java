package com.server.hearoad.Model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ChatMessage {
    private String type;
    private String message;
    private String ksl;
    private LocalDateTime timestamp;
}
