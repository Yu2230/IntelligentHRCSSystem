package com.yyds.hrcspojo.data.user;

import lombok.Data;

@Data
public class LoginDTO {
    private String email;
    private String password;
    private int role;
}
