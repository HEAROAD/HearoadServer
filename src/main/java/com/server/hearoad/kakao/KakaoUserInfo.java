package com.server.hearoad.kakao;

import java.util.Map;

public class KakaoUserInfo extends Oauth2UserInfo {
    public KakaoUserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public Long getId() {
        return Long.parseLong(String.valueOf(attributes.get("id")));
    }

    @Override
    public String getName() {
        return getKakaoAccount().get("name") != null ? (String) getKakaoAccount().get("name") : "No Name";  // null 체크 추가
    }

    @Override
    public String getNickName() {
        return getProfile().get("nickname") != null ? (String) getProfile().get("nickname") : "No Nickname";  // null 체크 추가
    }

    public Map<String, Object> getKakaoAccount() {
        return (Map<String, Object>) attributes.get("kakao_account");
    }

    public Map<String, Object> getProfile() {
        return (Map<String, Object>) getKakaoAccount().get("profile");
    }

    public String getProvider() {
        return "kakao";
    }
}
