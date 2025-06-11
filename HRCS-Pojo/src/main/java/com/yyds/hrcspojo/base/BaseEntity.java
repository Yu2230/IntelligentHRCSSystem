package com.yyds.hrcspojo.base;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
public class BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;  // 对应BIGINT PRIMARY KEY

    @TableField(fill = FieldFill.INSERT)
    private String createBy;  // 创建人ID

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;  // 修改人ID

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;  // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;  // 更新时间

    @Version
    @TableField(fill = FieldFill.INSERT)
    private int version;  // 版本号
}
