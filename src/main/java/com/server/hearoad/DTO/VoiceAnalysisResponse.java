package com.server.hearoad.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VoiceAnalysisResponse {

    @JsonProperty("character")
    private String character;

    @JsonProperty("nickname")
    private String nickname;

    // Getter와 Setter 추가
    public String getCharacter() {
        return character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
