package com.yyds.hrcspojo.department;

import com.yyds.hrcspojo.entity.Department;
import com.yyds.hrcspojo.entity.User;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Data
@Slf4j
@NoArgsConstructor
public class DepartmentWithUsersVO {
    private Department department;
    private List<User> userList;
}
