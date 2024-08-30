package com.server.hearoad.Service;

import com.server.hearoad.Model.Member;
import com.server.hearoad.Repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberServiceImp implements MemberService {
    @Autowired
    private MemberRepository memberRepository;

    @Override
    public List<Member> findAll(){
        return memberRepository.findAll();
    }

    @Override
    public Member findByEmail(String email){
        return memberRepository.findByEmail(email);
    }

    @Override
    public Member findById(String id) {
        return memberRepository.findById(id).orElse(null);
    }

    @Override
    public List<Member> findAllByOrderByNameDesc(){
        return memberRepository.findAllByOrderByNameDesc();
    }

    @Override
    public Member saveOrUpdateMember(Member member){
        return memberRepository.save(member);
    }

    @Override
    public void deleteMemberById(String id){
        memberRepository.deleteById(id);
    }
}
