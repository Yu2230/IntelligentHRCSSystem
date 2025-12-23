package com.yyds.hrcspojo.data.user.department;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.yyds.hrcspojo.entity.Department;
import com.yyds.hrcspojo.entity.User;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentVO {
    private Long id;
    private String departmentName;
    private Long managerId;
    private String description;
    private Integer status;
    private String createBy;
    private String updateBy;
    private Date createTime;
    private Date updateTime;

    // 新增字段
    private String managerName;
    private Integer memberCount;
    private List<User> userList;
}