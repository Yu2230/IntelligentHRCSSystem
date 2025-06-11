package com.yyds.hrcspojo.data.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginByCodeDTO {
    private String email;
    private String code;
    private int role;
}
