package com.server.hearoad;

import com.server.hearoad.Service.KakaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Controller
public class HomeController {
    private final KakaoService kakaoService;
    @GetMapping("/")
    public String login(Model model){
        model.addAttribute("kakaoUrl", kakaoService.getKakaoLogin());

        return "index";
    }

}
