package com.server.hearoad.Repository;

import com.server.hearoad.domain.KakaoMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KakaoMemberRepository extends JpaRepository<KakaoMember, Long> {
    KakaoMember save(KakaoMember kakaoMember);
}