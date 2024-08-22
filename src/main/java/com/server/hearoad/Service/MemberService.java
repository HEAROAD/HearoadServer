package com.server.hearoad.Service;

import com.server.hearoad.Model.Member;

import java.util.List;

public interface MemberService {
    List<Member> findAll();

    Member findByEmail(String email);

    Member findById(String id);

    List<Member> findAllByOrderByNameDesc();

    Member saveOrUpdateMember(Member member);

    void deleteMemberById(String id);
}
