package com.server.hearoad.kakao;

import java.util.Map;

public abstract class Oauth2UserInfo {
    protected Map<String, Object> attributes;

    public Oauth2UserInfo(Map<String, Object> attributes){
        this.attributes = attributes;
    }

    public Map<String, Object> getAttributes(){
        return attributes;
    }

    public abstract Long getId();
    public abstract String getName();
    public abstract String getNickName();
}
