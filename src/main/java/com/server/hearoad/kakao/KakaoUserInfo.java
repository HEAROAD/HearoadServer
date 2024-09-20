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
        return (String) getKakaoAccount().get("name");
    }

    @Override
    public String getNickName() {
        return (String) getProfile().get("nickname");
    }
    public Map<String, Object> getKakaoAccount(){
        return(Map<String, Object>) attributes.get("kakao_account");
    }

    public Map<String, Object> getProfile(){
        return (Map<String, Object>) getKakaoAccount().get("profile");
    }

    public String getProvider(){
        return "kakao";
    }

}
