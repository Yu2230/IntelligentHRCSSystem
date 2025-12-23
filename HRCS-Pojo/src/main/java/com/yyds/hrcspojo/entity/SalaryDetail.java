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
 * 工资明细表
 * @TableName salary_detail
 */
@TableName(value ="salary_detail")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SalaryDetail {
    /**
     * 明细ID
     */
    private Long id;

    /**
     * 工资主表ID
     */
    private Long salaryId;

    /**
     * 类型：0奖金 1扣除
     */
    private Integer commissionType;

    /**
     * 调整项名称
     */
    private String commissionName;

    /**
     * 调整金额
     */
    private BigDecimal commissionAmount;

    /**
     * 说明
     */
    private String description;

    /**
     * 创建人
     */
    private Long createBy;

    /**
     * 
     */
    private Date createTime;


}