package com.yyds.hrcspojo.login;

import com.yyds.hrcspojo.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LoginVO{
    private User user;
    private String token;
}
