package com.server.hearoad.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.hearoad.Model.User;
import com.server.hearoad.Repository.UserRepository;
import com.server.hearoad.Response.LoginResponse;
import com.server.hearoad.Tokens.AuthTokens;
import com.server.hearoad.Tokens.Generator.AuthTokensGenerator;
import com.server.hearoad.Tokens.Generator.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class KakaoService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final UserRepository userRepository;
    private final AuthTokensGenerator authTokensGenerator;
    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate;

    public LoginResponse kakaoLoginWithToken(String accessToken) {
        try {
            KakaoUserInfo userInfo = getKakaoUserInfo(accessToken);

            User user = userRepository.findById(userInfo.getId()).orElse(null);
            if (user == null) {
                user = new User();
                user.setId(userInfo.getId());
                user.setNickname(userInfo.getNickname());
                user.setLoginType("kakao");
                userRepository.save(user);
            }

            AuthTokens newToken = authTokensGenerator.generate(user.getId());
            return new LoginResponse(Long.parseLong(user.getId()), user.getNickname(), newToken);
        } catch (Exception e) {
            logger.error("Error processing Kakao login with token", e);
            throw new RuntimeException("Failed to process Kakao login", e);
        }
    }

    private KakaoUserInfo getKakaoUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(response.getBody());

                String id = rootNode.path("id").asText();
                String nickname = rootNode.path("properties").path("nickname").asText();

                return new KakaoUserInfo(id, nickname);
            } else {
                throw new RuntimeException("Failed to get Kakao user info. Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Error while getting Kakao user info", e);
            throw new RuntimeException("Failed to get Kakao user info", e);
        }
    }

    public String getUserIdFromToken(String jwtToken) {
        try {
            return jwtTokenProvider.getSubject(jwtToken);
        } catch (Exception e) {
            logger.error("Error getting user ID from token", e);
            return null;
        }
    }

    public String getUserNicknameFromToken(String jwtToken) {
        try {
            return jwtTokenProvider.getClaim(jwtToken, "nickname");
        } catch (Exception e) {
            logger.error("Error getting user nickname from token", e);
            return null;
        }
    }

    private static class KakaoUserInfo {
        private String id;
        private String nickname;

        public KakaoUserInfo(String id, String nickname) {
            this.id = id;
            this.nickname = nickname;
        }

        public String getId() {
            return id;
        }

        public String getNickname() {
            return nickname;
        }
    }
}