package com.server.hearoad.Controller;

import com.server.hearoad.DTO.KakaoUserProfileDto;
import com.server.hearoad.Service.KakaoService;
import com.server.hearoad.security.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("")
public class KakaoLoginReController {

    private final KakaoService kakaoService;
    private final JwtTokenUtil jwtTokenUtil;

    @GetMapping("/callback")
    public ResponseEntity<?> callback(@RequestParam("code") String code) {
        log.info("Callback endpoint hit with code: {}", code);

        String accessToken = kakaoService.getAccessTokenFromKakao(code);

        KakaoUserProfileDto userProfile = kakaoService.getUserProfile(accessToken);

        kakaoService.saveOrUpdateUser(userProfile);

        String jwtToken = jwtTokenUtil.generateToken(userProfile.getId().toString());

        return ResponseEntity.ok(new JwtResponse(jwtToken, userProfile.getNickname()));
    }

    public static class JwtResponse {
        private String token;
        private String username;

        public JwtResponse(String token, String username) {
            this.token = token;
            this.username = username;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }
}
