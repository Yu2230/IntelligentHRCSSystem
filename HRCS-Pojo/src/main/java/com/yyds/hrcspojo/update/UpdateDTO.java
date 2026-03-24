package com.yyds.hrcspojo.update;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class UpdateDTO implements Serializable {
    @NotBlank(message = "用户ID不能为空")
    private Long id;

    private String name;
    private String userName;
    private String workNo;
    private String phone;
    private Integer sex;
    private String avatar;
    private String address;
    private String idCard;
    private String bankCard;

    // 管理员专用
    private Integer role;
    private Integer post;
}