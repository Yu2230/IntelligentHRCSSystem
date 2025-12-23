package com.yyds.hrcspojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

/**
 * 公告附件表
 * @TableName notice_attachment
 */
@TableName(value ="notice_attachment")
public class NoticeAttachment {
    /**
     * 附件ID
     */
    private Long id;

    /**
     * 所属公告ID
     */
    private Long noticeId;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * OSS访问URL
     */
    private String fileUrl;

    /**
     * OSS对象名称（用于删除）
     */
    private String ossObjectName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件类型（doc/pdf等）
     */
    private String fileType;

    /**
     * 上传人
     */
    private String createBy;

    /**
     * 上传时间
     */
    private Date createTime;


}