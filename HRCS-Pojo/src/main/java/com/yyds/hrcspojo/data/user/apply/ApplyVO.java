package com.yyds.hrcspojo.data.user.apply;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
@Data
@NoArgsConstructor
public class ApplyVO {
    @ApiModelProperty("申请ID")
    private Long id;

    @ApiModelProperty("流程实例ID")
    private String processInstanceId;

    @ApiModelProperty("请假类型: 0病假, 1公假, 2私假")
    private Integer type;

    @ApiModelProperty("请假原因")
    private String reason;

    @ApiModelProperty("申请人ID")
    private Long applicantId;

    @ApiModelProperty("申请人姓名")
    private String applicantName;

    @ApiModelProperty("申请人角色: 0普通员工, 1部门负责人, 2管理员")
    private Integer applicantRole;

    @ApiModelProperty("部门ID")
    private Long departmentId;

    @ApiModelProperty("部门名称")
    private String departmentName;

    @ApiModelProperty("请假天数")
    private Integer days;

    @ApiModelProperty("请假开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @ApiModelProperty("请假结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    @ApiModelProperty("审批状态: 0审批中, 1已通过, 2已驳回")
    private Integer state;

    @ApiModelProperty("状态文本描述")
    private String stateText;

    @ApiModelProperty("当前审批人ID")
    private Long currentApproverId;

    @ApiModelProperty("当前审批人姓名")
    private String currentApproverName;

    @ApiModelProperty("当前审批人角色")
    private Integer currentApproverRole;

    @ApiModelProperty("当前审批节点名称")
    private String currentTaskName;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
