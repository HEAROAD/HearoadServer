package com.server.hearoad.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TTSResponse {
    private String message;
    private String filePath;
    private String emoji;
}