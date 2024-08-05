package com.server.hearoad.Repository;

import com.server.hearoad.Model.Member;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MemberRepository extends MongoRepository<Member, String> {

    Member findByEmail(String email);

    List<Member> findAllByOrderByNameDesc();
}
