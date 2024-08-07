package com.server.hearoad.Controller;

import com.server.hearoad.Repository.KakaoMemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.io.PrintWriter;

@Controller
public class KakaoMemberController {
    @Autowired
    private KakaoMemberRepository kakaoMemberRepository;

    @GetMapping("/show")
    private void  doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        PrintWriter out = response.getWriter();
        String name = "";

        HttpSession session = request.getSession();

        name = (String) session.getAttribute("login.name");
        out.print("<html><body>");
        out.print("이름 " + name + "<br>");
        out.print("<html><body>");
    }

}
