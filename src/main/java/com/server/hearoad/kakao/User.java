package com.server.hearoad.kakao;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "users")  // MongoDB 컬렉션명 지정
public class User {
    @Id
    private Long id;  // 카카오 PK 값
    private String name;
    private String nickName;
    private String kakaoAccessToken;
    private String kakaoRefreshToken;
    private String cloudEmail;
    private String calenderEmail;
    private LocalDateTime kakaoUpdate;
    private List<String> calenderList = new ArrayList<>();
    private String role;  // Role 필드 추가

    public User(Long id, String name, String nickName, String kakaoAccessToken, String kakaoRefreshToken, String role) {
        this.id = id;
        this.name = name;
        this.nickName = nickName;
        this.kakaoAccessToken = kakaoAccessToken;
        this.kakaoRefreshToken = kakaoRefreshToken;
        this.role = role;
    }

    public void setKakaoUpdate() {
        this.kakaoUpdate = LocalDateTime.now();
    }

    public void updateNickName(String nickName) {
        this.nickName = nickName;
    }

    public void setKakaoToken(String kakaoAccessToken, String kakaoRefreshToken) {
        this.kakaoAccessToken = kakaoAccessToken;
        this.kakaoRefreshToken = kakaoRefreshToken;
    }
}
