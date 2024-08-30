package com.server.hearoad.Controller;

import com.server.hearoad.DTO.KakaoUserProfileDto;
import com.server.hearoad.Service.KakaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

    @GetMapping("/callback")
    public ResponseEntity<?> callback(@RequestParam("code") String code) {
        log.info("Callback endpoint hit with code: {}", code);
        String accessToken = kakaoService.getAccessTokenFromKakao(code);
        KakaoUserProfileDto userProfile = kakaoService.getUserProfile(accessToken);
        kakaoService.saveOrUpdateUser(userProfile);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}