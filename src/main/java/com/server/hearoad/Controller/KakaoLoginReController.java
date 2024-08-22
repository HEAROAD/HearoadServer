package com.server.hearoad.Controller;

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
//        String accessToken = kakaoService.getAccessTokenFromKakao(code);
        //이거 ClientSecret때문에 활성화 아직 안됨 추 후에 비활성화 후 주석해제하기
        return new ResponseEntity<>(HttpStatus.OK);
        // 200 OK 상태 코드를 반환해줌
    }
    // 카카오 로그인 과정에서 사용자가 인증을 완료하면, 카카오 서버는 이 경로로 인증 코드를 반환
}
