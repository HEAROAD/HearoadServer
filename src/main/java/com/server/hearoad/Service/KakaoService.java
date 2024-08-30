package com.server.hearoad.Service;

import com.server.hearoad.DTO.KakaoTokenResponseDto;
import com.server.hearoad.DTO.KakaoUserProfileDto;
import com.server.hearoad.Model.User;
import com.server.hearoad.Repository.UserRepository;
import io.netty.handler.codec.http.HttpHeaderValues;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class KakaoService {

    private final UserRepository userRepository;

    @Value("${kakao.client-id}")
    private String clientId;

    private final WebClient webClient = WebClient.create();

    private final String KAUTH_TOKEN_URL_HOST = "https://kauth.kakao.com";
    private final String KAUTH_USER_URL_HOST = "https://kapi.kakao.com";

    public String getAccessTokenFromKakao(String code) {
        try {
            KakaoTokenResponseDto kakaoTokenResponseDto = WebClient.create(KAUTH_TOKEN_URL_HOST)
                    .post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/oauth/token")
                            .queryParam("grant_type", "authorization_code")
                            .queryParam("client_id", clientId)
                            .queryParam("code", code)
                            .build())
                    .header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                        log.error("Failed to retrieve access token from Kakao. Status: {}", clientResponse.statusCode());
                        return Mono.error(new RuntimeException("Failed to retrieve access token from Kakao."));
                    })
                    .bodyToMono(KakaoTokenResponseDto.class)
                    .block();

            log.info(" [Kakao Service] Access Token ------> {}", kakaoTokenResponseDto.getAccessToken());
            return kakaoTokenResponseDto.getAccessToken();
        } catch (WebClientResponseException e) {
            log.error("Error while retrieving access token: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve access token from Kakao.", e);
        }
    }

    public KakaoUserProfileDto getUserProfile(String accessToken) {
        try {
            KakaoUserProfileDto userProfile = WebClient.create(KAUTH_USER_URL_HOST)
                    .get()
                    .uri("/v2/user/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                        log.error("Failed to retrieve user profile from Kakao. Status: {}", clientResponse.statusCode());
                        return Mono.error(new RuntimeException("Failed to retrieve user profile from Kakao."));
                    })
                    .bodyToMono(KakaoUserProfileDto.class)
                    .block();

            log.info("User profile retrieved successfully.");
            return userProfile;
        } catch (WebClientResponseException e) {
            log.error("Error while retrieving user profile: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve user profile from Kakao.", e);
        }
    }

    public User saveOrUpdateUser(KakaoUserProfileDto userProfile) {
        log.info("saveOrUpdateUser method called");
        try {
            String nickname = userProfile.getProperties().getNickname();
            String profileImage = userProfile.getProperties().getProfileImage();

            log.info("User Nickname: {}", nickname);
            log.info("User Profile Image: {}", profileImage);

            User user = userRepository.findByNickname(nickname)
                    .orElseGet(() -> new User(nickname, profileImage));

            log.info("User found or new user created");

            user.setNickname(nickname);
            user.setProfileImage(profileImage);

            User savedUser = userRepository.save(user);
            log.info("User saved successfully.");
            return savedUser;
        } catch (Exception e) {
            log.error("Error occurred while saving user: ", e);
            throw new RuntimeException("Failed to save or update user.", e);
        }
    }
}
