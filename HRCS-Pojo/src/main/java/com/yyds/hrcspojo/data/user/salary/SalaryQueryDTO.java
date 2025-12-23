package com.yyds.hrcspojo.data.user.salary;

import lombok.Data;

@Data
public class SalaryQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String userName; // 模糊查询
    private Long deptId;
    private String month; // yyyy-MM
    private Integer status; // 0未发 1已发
    private Long userId;
}