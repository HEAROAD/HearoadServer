package com.server.hearoad.Tokens.Generator;

import com.server.hearoad.Tokens.AuthTokens;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class AuthTokensGenerator {

    private static final String BEARER_TYPE = "Bearer";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24; //일단 24시간
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 30;  // 일단 30일

    private final JwtTokenProvider jwtTokenProvider;

    // id와 nickname을 받아 Access Token 생성
    public AuthTokens generate(String uid, String nickname) {
        long now = (new Date()).getTime();
        Date accessTokenExpiredAt = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
        Date refreshTokenExpiredAt = new Date(now + REFRESH_TOKEN_EXPIRE_TIME);

        // AccessToken과 RefreshToken 생성
        String accessToken = jwtTokenProvider.accessTokenGenerate(uid, nickname, accessTokenExpiredAt);
        String refreshToken = jwtTokenProvider.refreshTokenGenerate(refreshTokenExpiredAt);

        // AuthTokens 반환
        return AuthTokens.of(accessToken, refreshToken, BEARER_TYPE, ACCESS_TOKEN_EXPIRE_TIME / 1000L);
    }

    // 기존 메소드 오버로딩 (하위 호환성 유지)
    public AuthTokens generate(String uid) {
        return generate(uid, null);
    }
}