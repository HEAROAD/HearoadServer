package com.server.hearoad.DTO;

import com.server.hearoad.Model.Member;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberDTO {
    private String id;
    private String name;
    private String email;
    private String password;
}
