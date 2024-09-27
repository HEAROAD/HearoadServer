package com.server.hearoad.DTO;

public class UserProfileResponse {
    private String nickname;
    private String character; // 분석된 캐릭터 정보, 없을 경우 "히로"

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
