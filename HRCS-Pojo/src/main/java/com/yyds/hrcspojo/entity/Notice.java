package com.yyds.hrcspojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 公告表
 * @TableName notice
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value ="notice")
public class Notice {
    /**
     * 主键ID
     */
    private Long id;
    private Long departmentId;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告内容
     */
    private String content;

    /**
     * 公告类型: 1-系统公告 2-部门公告
     */
    private Integer type;

    /**
     * 状态: 1-草稿 2-已发布 3-已下架
     */
    private Integer status;

    /**
     * 优先级: 1-普通 2-重要
     */
    private Integer priority;

    /**
     * 发布时间
     */
    private Date publishTime;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 逻辑删除: 0-未删除 1-已删除
     */
    private Integer deleted;
    // 新增：需要同步到数据库
    private String fileUrl;          // 数据库需添加 file_url 列
    private String ossObjectName;    // 数据库需添加 oss_object_name 列

    @TableField(exist = false)
    private List<NoticeAttachment> attachments;
    @TableField(exist = false)
    private List<User> receiverList;  // 接收人列表（仅用于业务逻辑）

    @TableField(exist = false)
    private Boolean isExpired;

}