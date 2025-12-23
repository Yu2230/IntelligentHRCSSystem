package com.yyds.hrcspojo.data.user.salary;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalaryDetailDTO {
    private Long salaryId;
    private Integer commissionType; // 0奖金 1扣除
    private String commissionName;
    private BigDecimal commissionAmount;
    private String description;
    private Long operatorId; // 操作人ID
}