package com.server.hearoad.Kakao;

public interface OauthMemberClient {

    OauthServerType supportServer();

    OauthMember fetch(String code);
}