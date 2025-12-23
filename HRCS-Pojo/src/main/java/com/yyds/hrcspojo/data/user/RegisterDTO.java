package com.yyds.hrcspojo.data.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterDTO {
    @Email(message = "email is incorrect")
    private String email;
    @Size(max = 6, min = 6, message = "code is incorrect")
    private String code;
    @Size(max = 12,min = 8, message = "password is incorrect")
    private String password;

    @NotNull
    private String userName;
    @NotNull
    private int sex;
    @Size(max = 11,  min = 11, message = "phoneNumber is incorrect")
    private String phone;
    @NotNull
    private String address;
}
