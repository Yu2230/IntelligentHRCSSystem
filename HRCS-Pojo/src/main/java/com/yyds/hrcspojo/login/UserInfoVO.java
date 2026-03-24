package com.yyds.hrcspojo.login;

import com.yyds.hrcspojo.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoVO {
    private User user;
    private String departmentName;
}
