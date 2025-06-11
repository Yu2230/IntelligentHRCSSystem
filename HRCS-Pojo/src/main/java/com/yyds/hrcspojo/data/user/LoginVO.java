package com.yyds.hrcspojo.data.user;

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
