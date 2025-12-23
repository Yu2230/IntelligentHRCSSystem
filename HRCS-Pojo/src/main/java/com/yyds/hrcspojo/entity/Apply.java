package com.yyds.hrcspojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import nonapi.io.github.classgraph.json.Id;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import java.util.Date;

/**
 * 请假申请表
 * @TableName apply
 */
@TableName(value ="apply")
@Data
@EntityScan("com.yyds.hrcsserver.user.entity")
public class Apply {
    /**
     * ID
     */
    @Id
    private Long id;

    /**
     * Activiti流程实例ID
     */
    private String processInstanceId;

    /**
     * 请假类型: 0病假, 1公假, 2私假
     */
    private Integer type;

    /**
     * 请假原因
     */
    private String reason;

    /**
     * 申请人ID
     */
    private Long applicantId;
    private Long departmentId;


    /**
     * 当前审批人ID（关键字段：用于非Activiti流程或作为冗余字段）
     */
    private Long currentApproverId;


    /**
     * 请假天数
     */
    private Integer days;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 状态: 0审批中, 1已通过, 2已驳回
     */
    private Integer state;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;


}