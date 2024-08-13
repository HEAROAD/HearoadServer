package com.server.hearoad.Kakao.Oauth;

public interface AuthCodeRequestUrlProvider {
    OauthServerType supportServer();

    String provide();
}
