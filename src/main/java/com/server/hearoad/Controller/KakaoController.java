package com.server.hearoad.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
@RequestMapping("/")
public class KakaoController {

    @GetMapping("/login")
    public String loginPage() {
        // 원래 /oauth/kakao로 리디렉션을 했지만, 이제는 Thymeleaf 템플릿으로 로그인 페이지를 렌더링
        return "login";  // login.html 템플릿을 반환
    }

    @GetMapping("/kakao/callback")
    public String kakaoRedirectPage() {
        // 카카오 인증 후 리디렉션된 사용자를 처리하기 위해 kakaoRedirect.html을 렌더링
        return "kakaoRedirect";  // kakaoRedirect.html 템플릿을 반환
    }

    @GetMapping("/success")
    public String successPage() {
        // 로그인 성공 시 이동할 페이지 (success.html 템플릿을 반환)
        return "success";
    }

    @GetMapping("/fail")
    public String failPage() {
        // 로그인 실패 시 이동할 페이지 (fail.html 템플릿을 반환)
        return "fail";
    }
}
