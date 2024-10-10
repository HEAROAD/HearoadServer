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

import java.util.HashMap;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class KakaoService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final UserRepository userRepository;
    private final AuthTokensGenerator authTokensGenerator;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponse kakaoLoginWithToken(String accessToken) {
        // 1. 액세스 토큰으로 카카오 API 호출하여 사용자 정보 가져오기
        HashMap<String, Object> userInfo = getKakaoUserInfo(accessToken);

        // 2. 카카오 ID로 회원가입 및 로그인 처리
        return kakaoUserLogin(userInfo);
    }

    // 토큰으로 카카오 API 호출하여 사용자 정보 가져오기
    private HashMap<String, Object> getKakaoUserInfo(String accessToken) {
        HashMap<String, Object> userInfo = new HashMap<>();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<String> kakaoUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                kakaoUserInfoRequest,
                String.class
        );

        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(responseBody);
            Long id = jsonNode.get("id").asLong();
            String nickname = jsonNode.get("properties").get("nickname").asText();

            userInfo.put("id", id);
            userInfo.put("nickname", nickname);
        } catch (Exception e) {
            logger.error("Error parsing user info from Kakao", e);
            throw new RuntimeException("Failed to retrieve user info from Kakao");
        }

        return userInfo;
    }

    // 카카오 ID로 회원가입 및 로그인 처리
    private LoginResponse kakaoUserLogin(HashMap<String, Object> userInfo) {
        Long uid = Long.valueOf(userInfo.get("id").toString());
        String nickName = userInfo.get("nickname").toString();

        User kakaoUser = userRepository.findById(uid.toString()).orElse(null);

        if (kakaoUser == null) {    // 회원가입
            kakaoUser = new User();
            kakaoUser.setId(uid.toString());
            kakaoUser.setNickname(nickName);
            kakaoUser.setLoginType("kakao");
            userRepository.save(kakaoUser);
        }

        // 토큰 생성
        AuthTokens token = authTokensGenerator.generate(uid.toString());
        return new LoginResponse(uid, nickName, token);
    }

    // JWT 토큰에서 사용자 ID를 추출하는 메서드
    public String getUserIdFromToken(String token) {
        return jwtTokenProvider.getSubject(token);
    }

    public String getUserNicknameFromToken(String accessToken) {
        try {
            // JWT 토큰에서 사용자 ID 추출
            String userId = jwtTokenProvider.getSubject(accessToken);

            // 사용자 정보 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("User not found"));

            return user.getNickname();
        } catch (Exception e) {
            logger.error("Error getting user nickname from token", e);
            return null;
        }
    }
}