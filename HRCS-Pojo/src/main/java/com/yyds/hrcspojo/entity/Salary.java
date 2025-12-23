package com.yyds.hrcspojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 工资表
 * @TableName salary
 */
@TableName(value ="salary")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Salary {
    private Long id;

    /**
     * 员工ID
     */
    private Long userId;

    /**
     * 员工姓名（冗余）
     */
    private String userName;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 工资月份：yyyy-MM
     */
    private String month;

    /**
     * 基础工资
     */
    private BigDecimal baseSalary;

    /**
     * 总调整金额（奖金-扣除）
     */
    private BigDecimal totalCommission;

    /**
     * 实发工资
     */
    private BigDecimal actualSalary;

    /**
     * 发放状态：0未发放 1已发放
     */
    private Integer status;

    /**
     * 发放人ID
     */
    private Long issuerId;

    /**
     * 发放时间
     */
    private Date issueTime;

    /**
     * 创建人
     */
    private Long createBy;

    /**
     * 更新人
     */
    private Long updateBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

}