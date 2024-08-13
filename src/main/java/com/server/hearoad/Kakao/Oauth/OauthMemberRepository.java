package com.server.hearoad.Kakao.Oauth;

import java.util.Optional;

import com.server.hearoad.Kakao.Oauth.OauthId;
import com.server.hearoad.Kakao.Oauth.OauthMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OauthMemberRepository extends JpaRepository<OauthMember, Long> {

    Optional<OauthMember> findByOauthId(OauthId oauthId);
}
