package com.server.hearoad.DTO;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "tts_files")
public class TTSFile {

    @Id
    private String id;
    private String userId;
    private String word;
    private String emoji;
    private String filePath;
}
