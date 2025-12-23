package com.yyds.hrcspojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 工资调整规则表
 * @TableName commission_rule
 */
@TableName(value ="commission_rule")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommissionRule {
    /**
     * 
     */
    private Long id;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 0奖金 1扣除
     */
    private Integer commissionType;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 触发条件类型
     */
    private String conditionType;

    /**
     * 触发条件值
     */
    private String conditionValue;

    /**
     * 适用月份
     */
    private String applicableMonth;

    /**
     * 
     */
    private Long createBy;

    /**
     * 
     */
    private Date createTime;


}