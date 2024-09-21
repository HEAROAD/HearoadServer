package com.server.hearoad.Controller;

import com.server.hearoad.Service.KakaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")

public class UserController {
    private final KakaoService kakaoService;

    @ResponseBody
    @GetMapping("/login/oauth/kakao")
    public ResponseEntity<LoginResponse> kakaoLoginFromAndroid(@RequestParam String code) {
        try {
            return ResponseEntity.ok(kakaoService.kakaoLogin(code, "android"));
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item Not Found");
        }
    }
}