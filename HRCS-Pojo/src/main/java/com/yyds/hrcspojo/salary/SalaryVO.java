package com.yyds.hrcspojo.salary;

import com.yyds.hrcspojo.entity.SalaryDetail;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SalaryVO {
    private Long id;
    private Long userId;
    private String userName;
    private Long deptId;
    private String deptName;
    private String month;
    private BigDecimal baseSalary;
    private BigDecimal totalCommission;
    private BigDecimal actualSalary;
    private Integer status;
    private String statusText;
    private Long issuerId;
    private LocalDateTime issueTime;
    private List<SalaryDetail> detailList;
}
