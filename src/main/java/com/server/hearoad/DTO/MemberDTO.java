package com.server.hearoad.DTO;

import com.server.hearoad.Model.Member;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberDTO {

    public String id;
    public String name;
    public String email;
    public String password;

    public MemberDTO(){
    }
}
