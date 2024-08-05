package com.server.hearoad.Controller;

import com.server.hearoad.DTO.MemberDTO;
import com.server.hearoad.Model.Member;
import com.server.hearoad.Service.MemberServiceImp;
import com.server.hearoad.Service.util.ObjectMapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/register")
public class MemberController {

    @Autowired
    private MemberServiceImp memberService;

    @GetMapping(value = "/")
    public List<MemberDTO> getAllMembers() {
        return ObjectMapperUtils.mapAll(memberService.findAll(), MemberDTO.class);
    }

    @GetMapping(value = "byEmail/{email}")
    public MemberDTO getMemberByEmail(@PathVariable("email") String email) {
        return ObjectMapperUtils.map(memberService.findByEmail(email), MemberDTO.class);
    }

    @GetMapping(value = "/orderByNameDesc")
    public List<MemberDTO> findAllByOrderByNameDesc() {
        return ObjectMapperUtils.mapAll(memberService.findAllByOrderByNameDesc(), MemberDTO.class);
    }

    @PostMapping(value = "/save")
    public ResponseEntity<?> saveOrUpdateMember(@RequestBody MemberDTO memberDTO) {
        Member member = memberService.findByEmail(memberDTO.getEmail());
        String responseMessage = "Member added success";
        if (member != null && member.getId() != null && member.getId().length() > 0) {
            responseMessage = "This Email already Exist";
        } else {
            memberService.saveOrUpdateMember(ObjectMapperUtils.map(memberDTO, Member.class));
        }
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    @PostMapping(value = "/delete/{email}")
    public ResponseEntity<?> deleteMemberByEmail(@PathVariable String email) {
        Member member = memberService.findByEmail(email);
        String responseMessage = "Member Deleted success";
        if (member != null && member.getId() != null && member.getId().length() > 0) {
            memberService.deleteMemberById(memberService.findByEmail(email).getId());
        } else {
            responseMessage = "This Email Does Not exist in our Member List";
        }
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }
}
