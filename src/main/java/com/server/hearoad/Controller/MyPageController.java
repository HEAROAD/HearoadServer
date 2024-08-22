package com.server.hearoad.Controller;

import com.server.hearoad.DTO.MemberDTO;
import com.server.hearoad.Model.Member;
import com.server.hearoad.Service.MemberServiceImp;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mypage")
public class MyPageController {

    @Autowired
    private MemberServiceImp memberService;

    @GetMapping("/info")
    public ResponseEntity<MemberDTO> getMemberInfo(HttpSession session) {
        String id = (String) session.getAttribute("userId");

        if (id == null) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }

        Member member = memberService.findById(id);
        if (member != null) {
            MemberDTO memberDTO = new MemberDTO();
            memberDTO.setId(member.getId());
            memberDTO.setName(member.getName());
            memberDTO.setEmail(member.getEmail());
            return new ResponseEntity<>(memberDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }


}
