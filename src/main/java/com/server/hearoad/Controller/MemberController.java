package com.server.hearoad.Controller;

import com.server.hearoad.DTO.MemberDTO;
import com.server.hearoad.Model.Member;
import com.server.hearoad.Service.MemberServiceImp;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/register")
public class MemberController {

    @Autowired
    private MemberServiceImp memberService;



    @PostMapping(value = "/save")
    public ResponseEntity<String> saveOrUpdateMember(@RequestBody MemberDTO memberDTO) {
        Member existingMember = memberService.findByEmail(memberDTO.getEmail());
        if (existingMember != null) {
            return new ResponseEntity<>("This Email already exists", HttpStatus.BAD_REQUEST);
        } else {
            Member member = new Member();
            member.setName(memberDTO.getName());
            member.setEmail(memberDTO.getEmail());
            member.setPassword(memberDTO.getPassword()); // 평문 비밀번호 그대로 저장
            memberService.saveOrUpdateMember(member);
            return new ResponseEntity<>("Member added successfully", HttpStatus.OK);
        }
    }

    @PostMapping(value = "/login")
    public ResponseEntity<String> loginMember(@RequestBody MemberDTO memberDTO, HttpSession session) {
        Member member = memberService.findByEmail(memberDTO.getEmail());
        if (member == null) {
            return new ResponseEntity<>("Invalid email", HttpStatus.UNAUTHORIZED);
        } else if (!memberDTO.getPassword().equals(member.getPassword())) {
            return new ResponseEntity<>("Invalid password", HttpStatus.UNAUTHORIZED);
        } else {
            // 로그인 성공 시 ID를 세션에 저장
            session.setAttribute("userId", member.getId());
            return new ResponseEntity<>("Login successful", HttpStatus.OK);
        }
    }


}
