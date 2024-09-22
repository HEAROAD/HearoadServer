package com.server.hearoad.Controller;

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
}
