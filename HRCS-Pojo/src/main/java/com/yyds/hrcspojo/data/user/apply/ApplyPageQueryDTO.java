package com.yyds.hrcspojo.data.user.apply;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;

// dto/ApplyPageQueryDTO.java
@Data
public class ApplyPageQueryDTO {
    @ApiModelProperty("页码，从1开始")
    private Integer pageNum = 1;

    @ApiModelProperty("每页条数")
    private Integer pageSize = 10;

    @ApiModelProperty("申请人ID")
    private Long applicantId;
    private Long departmentId;

    @ApiModelProperty("申请人姓名（模糊查询）")
    private String applicantName;

    @ApiModelProperty("状态：0待审批,1部门负责人已同意,2管理员已同意,3驳回")
    private Integer state;

    @ApiModelProperty("开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @ApiModelProperty("结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}