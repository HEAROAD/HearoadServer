package com.server.hearoad.kakao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OauthService {
    private static final String BEARER_TYPE = "Bearer";
    private final InMemoryClientRegistrationRepository inMemoryRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public LoginResponse login(String providerName, String code) {
        ClientRegistration provider = inMemoryRepository.findByRegistrationId(providerName);
        OAuth2AccessTokenResponse tokenResponse = getToken(code, provider);
        User user = getUserProfile(providerName, tokenResponse, provider);

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(user.getId()));
        String refreshToken = jwtTokenProvider.createRefreshToken();

        return new LoginResponse(user, accessToken, refreshToken);
    }

    private OAuth2AccessTokenResponse getToken(String code, ClientRegistration provider) {
        return WebClient.create()
                .post()
                .uri(provider.getProviderDetails().getTokenUri())
                .headers(httpHeaders -> {
                    httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    httpHeaders.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));
                })
                .bodyValue(tokenRequest(code, provider))
                .retrieve()
                .bodyToMono(OAuth2AccessTokenResponse.class)
                .block();
    }

    private MultiValueMap<String, String> tokenRequest(String code, ClientRegistration provider) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", code);
        formData.add("grant_type", "authorization_code");
        formData.add("redirect_uri", provider.getRedirectUri());
        formData.add("client_id", provider.getClientId());
        return formData;
    }

    private User getUserProfile(String providerName, OAuth2AccessTokenResponse tokenResponse, ClientRegistration provider) {
        Map<String, Object> userAttributes = getUserAttributes(provider, tokenResponse);

        if (!providerName.equals("kakao")) {
            throw new IllegalArgumentException("잘못된 접근입니다.");
        }

        KakaoUserInfo kakaoUserInfo = new KakaoUserInfo(userAttributes);
        Long kakao_id = kakaoUserInfo.getId();
        String name = kakaoUserInfo.getName();
        String nickName = kakaoUserInfo.getNickName();

        if (userRepository.findById(kakao_id).isPresent()) {
            throw new DuplicateSignInException();
        }

        // MongoDB에 저장할 User 객체 생성
        User user = new User(kakao_id, name, nickName, tokenResponse.getAccessToken().getTokenValue(), tokenResponse.getRefreshToken().getTokenValue(), "ROLE_USER");  // "ROLE_USER" 추가
        return userRepository.save(user);  // MongoDB에 저장
    }

    private Map<String, Object> getUserAttributes(ClientRegistration provider, OAuth2AccessTokenResponse tokenResponse) {
        return WebClient.create()
                .get()
                .uri(provider.getProviderDetails().getUserInfoEndpoint().getUri())
                .headers(httpHeaders -> httpHeaders.setBearerAuth(tokenResponse.getAccessToken().getTokenValue()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
    }
}