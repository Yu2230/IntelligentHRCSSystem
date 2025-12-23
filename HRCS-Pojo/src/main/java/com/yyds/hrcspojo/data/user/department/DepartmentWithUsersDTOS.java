package com.yyds.hrcspojo.data.user.department;

import com.yyds.hrcspojo.entity.User;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Data
@Builder
@Slf4j
public class DepartmentWithUsersDTOS {
    private Long departmentId;  // 添加部门ID字段
    private List<User> users;
}
