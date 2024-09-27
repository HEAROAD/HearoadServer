package com.server.hearoad.DTO;

public class UserProfileResponse {

    private String nickname;

    public UserProfileResponse(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}

