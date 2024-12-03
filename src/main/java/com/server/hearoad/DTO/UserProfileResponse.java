package com.server.hearoad.DTO;

public class UserProfileResponse {
    private String nickname;
    private String character;

    public UserProfileResponse(String nickname, String character) {
        this.nickname = nickname;
        this.character = character;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getCharacter() {
        return character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }
}
