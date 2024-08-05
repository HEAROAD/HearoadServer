package com.server.hearoad.Model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "members")
@Getter
@Setter
public class Member {
    @Id
    private String id;
    private String name;
    private String email;
    private String password;

}
