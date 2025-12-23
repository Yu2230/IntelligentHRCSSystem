package com.yyds.hrcspojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * 部门表
 * @TableName department
 */
@TableName(value ="department")
@Data
@Builder
public class Department {
    /**
     * 部门ID
     */

    private Long id;

    /**
     * 部门名称
     */
    private String departmentName;

    /**
     * 部门负责人ID
     */
    private Long managerId;

    /**
     * 部门描述
     */
    private String description;

    /**
     * 部门状态：1-启用 0-禁用
     */
    private Integer status;


    @TableField(fill = FieldFill.INSERT)
    private String createBy;  // 创建人ID

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;  // 修改人ID

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;  // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;  // 更新时间


}