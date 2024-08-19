package com.server.hearoad.Kakao;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.kakao")
public record KakaoOauthConfig(
        String redirectUri,
        String clientId,
        String clientSecret,
        String[] scope
) {
    // 기본 scope를 설정
    public KakaoOauthConfig {
        if (scope == null) {
            scope = new String[]{"profile_nickname", "profile_image"};
        }
    }
}

