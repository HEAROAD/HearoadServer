package com.server.hearoad.Service;

import com.server.hearoad.Repository.KakaoMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class KakaoMemberService {
    private final KakaoMemberRepository kakaoMemberRepository;
}
