package com.server.hearoad.kakao;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginResponse {
    private Long id;
    private String name;
    private String nickName;
    private String role;
    private String accessToken;
    private String refreshToken;

    public LoginResponse(User user, String accessToken, String refreshToken){
        this.id = user.getId();
        this.name = user.getName();
        this.nickName = user.getNickName();
        this.role = user.getRole();
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}

