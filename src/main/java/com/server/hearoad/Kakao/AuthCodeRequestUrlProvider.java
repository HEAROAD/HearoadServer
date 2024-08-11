package com.server.hearoad.Kakao;

import com.server.hearoad.Kakao.OauthServerType;

public interface AuthCodeRequestUrlProvider {
    OauthServerType supportServer();

    String provide();
}
