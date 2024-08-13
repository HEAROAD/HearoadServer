package com.server.hearoad.Kakao.Oauth;

import com.server.hearoad.Kakao.KakaoApiClient;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.toMap;

@Component
public class OauthMemberClientComposite {

    private final Map<OauthServerType, KakaoApiClient.OauthMemberClient> mapping;

    public OauthMemberClientComposite(Set<KakaoApiClient.OauthMemberClient> clients) {
        mapping = clients.stream()
                .collect(toMap(
                        KakaoApiClient.OauthMemberClient::supportServer,
                        identity()
                ));
    }

    public OauthMember fetch(OauthServerType oauthServerType, String authCode) {
        return getClient(oauthServerType).fetch(authCode);
    }

    private KakaoApiClient.OauthMemberClient getClient(OauthServerType oauthServerType) {
        return Optional.ofNullable(mapping.get(oauthServerType))
                .orElseThrow(() -> new RuntimeException("지원하지 않는 소셜 로그인 타입입니다."));
    }
}
