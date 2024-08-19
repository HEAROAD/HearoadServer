package com.server.hearoad.Controller;

import com.server.hearoad.DTO.MemberDTO;
import com.server.hearoad.Model.Member;
import com.server.hearoad.Service.MemberServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mypage")
public class MyPageController {

    @Autowired
    private MemberServiceImp memberService;

    @GetMapping("/info")
    public ResponseEntity<MemberDTO> getMemberInfo() {
        // 현재 인증된 사용자의 정보를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // 이메일이 유저네임으로 사용되었다고 가정

        Member member = memberService.findByEmail(email);
        if (member != null) {
            // 이름과 이메일만 포함된 DTO를 반환
            MemberDTO memberDTO = new MemberDTO();
            memberDTO.setName(member.getName());
            memberDTO.setEmail(member.getEmail());
            return new ResponseEntity<>(memberDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
}
