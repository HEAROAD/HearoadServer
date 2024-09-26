package com.server.hearoad.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.hearoad.Model.User;
import com.server.hearoad.Repository.UserRepository;
import com.server.hearoad.Response.LoginResponse;
import com.server.hearoad.Tokens.AuthTokens;
import com.server.hearoad.Tokens.Generator.AuthTokensGenerator;
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

    @Value("${kakao.key.client-id}")
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
            logger.error("Error parsing JSON", e);
        }

        if (jsonNode != null && jsonNode.has("access_token")) {
            return jsonNode.get("access_token").asText();
        } else {
            throw new RuntimeException("Failed to retrieve access token from Kakao");
        }
    }

    public String getUserNicknameFromToken(String accessToken) {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        HttpEntity<LinkedMultiValueMap<String, String>> kakaoProfileRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v2/user/me", // 카카오 사용자 정보 API
                HttpMethod.GET,
                kakaoProfileRequest,
                String.class
        );

        // JSON 응답에서 닉네임 추출
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(response.getBody());
            String nickname = jsonNode.get("nickname").asText();
            return nickname;
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 오류가 발생하면 null을 반환합니다.
        }
    }

    // 2. 토큰으로 카카오 API 호출
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
            logger.error("Error parsing user info from Kakao", e);
        }

        if (jsonNode != null) {
            Long id = jsonNode.get("id").asLong();
            String nickname = jsonNode.get("properties").get("nickname").asText();

            userInfo.put("id", id);
            userInfo.put("nickname", nickname);
        } else {
            throw new RuntimeException("Failed to retrieve user info from Kakao");
        }

        return userInfo;
    }

    // 3. 카카오ID로 회원가입 및 로그인 처리
    private LoginResponse kakaoUserLogin(HashMap<String, Object> userInfo) {
        Long uid = Long.valueOf(userInfo.get("id").toString());
        String nickName = userInfo.get("nickname").toString();

        User kakaoUser = userRepository.findById(uid.toString()).orElse(null);

        if (kakaoUser == null) {    // 회원가입
            kakaoUser = new User();
            kakaoUser.setId(uid.toString());  // 고유 ID 설정
            kakaoUser.setNickname(nickName);
            kakaoUser.setLoginType("kakao");
            userRepository.save(kakaoUser);
        }

        // 토큰 생성
        AuthTokens token = authTokensGenerator.generate(uid.toString());
        return new LoginResponse(uid, nickName, token);
    }

    // Web 버전 카카오 로그인 처리
    public LoginResponse kakaoLogin(String code, String currentDomain) {
        // 1. 인가 코드로 액세스 토큰 요청
        String accessToken = getAccessToken(code, redirectUri);

        // 2. 토큰으로 카카오 API 호출
        HashMap<String, Object> userInfo = getKakaoUserInfo(accessToken);

        // 3. 카카오 ID로 회원가입 및 로그인 처리
        return kakaoUserLogin(userInfo);
    }
}
