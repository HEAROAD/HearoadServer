package com.server.hearoad.Controller;

import com.server.hearoad.DTO.UserProfileResponse;
import com.server.hearoad.Model.User;
import com.server.hearoad.Repository.UserRepository;
import com.server.hearoad.Service.KakaoService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.server.hearoad.Response.LoginResponse;


import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final KakaoService kakaoService;
    private final UserRepository userRepository;

    @ResponseBody
    @PostMapping("/login/oauth/kakao")
    public ResponseEntity<LoginResponse> kakaoLoginFromAndroid(@RequestParam String code, HttpServletRequest request) {
        try {
            String currentDomain = request.getServerName();
            return ResponseEntity.ok(kakaoService.kakaoLogin(code, currentDomain));
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item Not Found");
        }
    }

    @GetMapping("/mypage")
    public ResponseEntity<UserProfileResponse> getUserInfo(HttpServletRequest request) {
        try {
            // JWT 토큰에서 사용자 ID 추출
            String accessToken = request.getHeader("Authorization").substring(7); // "Bearer " 제거
            String userId = kakaoService.getUserIdFromToken(accessToken);

            // 사용자 정보 조회
            User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found"));

            // 사용자 프로필 생성
            UserProfileResponse response = new UserProfileResponse(user.getNickname());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
    }


}
