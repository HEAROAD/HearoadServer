package com.server.hearoad.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.hearoad.Model.User;
import com.server.hearoad.Repository.UserRepository;
import com.server.hearoad.Response.LoginResponse;
import com.server.hearoad.Tokens.AuthTokens;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class KakaoService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final UserRepository userRepository;
    private final AuthTokensGenerator authTokensGenerator;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    // 1. "인가 코드"로 "액세스 토큰" 요청
    private String getAccessToken(String code, String redirectUri) {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(body, headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );

        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(responseBody);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonNode.get("access_token").asText();
    }

    // 2. 토큰으로 카카오 API 호출 - 사용자 정보 요청
    private HashMap<String, Object> getKakaoUserInfo(String accessToken) {
        HashMap<String, Object> userInfo = new HashMap<>();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoUserInfoRequest,
                String.class
        );

        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(responseBody);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Long id = jsonNode.get("id").asLong();
        String nickname = jsonNode.get("properties").get("nickname").asText();

        userInfo.put("id", id);
        userInfo.put("nickname", nickname);

        return userInfo;
    }

    // 3. 카카오ID로 회원가입 & 로그인 처리
    private LoginResponse kakaoUserLogin(HashMap<String, Object> userInfo) {

        Long uid = Long.valueOf(userInfo.get("id").toString());
        String nickName = userInfo.get("nickname").toString();

        // 카카오 ID로 기존 유저 조회
        User kakaoUser = userRepository.findById(uid.toString()).orElse(null);

        if (kakaoUser == null) {    // 회원가입
            kakaoUser = new User();
            kakaoUser.setId(uid.toString());  // 카카오 ID를 사용
            kakaoUser.setNickname(nickName);
            userRepository.save(kakaoUser);
        }

        // 토큰 생성
        AuthTokens token = authTokensGenerator.generate(uid.toString());
        return new LoginResponse(uid, nickName, token);
    }
}
