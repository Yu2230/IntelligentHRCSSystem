package com.yyds.hrcspojo.salary;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MonthlyStatVO {
    private String month;
    private Long totalCount;
    private BigDecimal totalAmount;
    private BigDecimal avgAmount;
}