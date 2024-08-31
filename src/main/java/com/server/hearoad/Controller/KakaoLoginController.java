
package com.server.hearoad.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/api/users/login")
public class KakaoLoginController {

    @GetMapping("/page")
    public String loginPage(
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            Model model) {

        String location = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id="
                + clientId + "&redirect_uri=" + redirectUri;

        model.addAttribute("location", location);

        return "login";
    }
}
