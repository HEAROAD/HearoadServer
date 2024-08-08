package com.server.hearoad.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
public class KakaoMember {
    @Id
    @GeneratedValue
    @Column(name="member_id")
    private Long member_id;

    private String username;
}