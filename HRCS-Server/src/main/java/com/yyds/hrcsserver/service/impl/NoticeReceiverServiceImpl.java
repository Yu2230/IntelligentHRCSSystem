package com.yyds.hrcsserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyds.hrcspojo.entity.NoticeReceiver;

import com.yyds.hrcsserver.mapper.NoticeReceiverMapper;
import com.yyds.hrcsserver.service.NoticeReceiverService;
import org.springframework.stereotype.Service;

/**
* @author 21641
* @description 针对表【notice_receiver(公告接收人表)】的数据库操作Service实现
* @createDate 2025-12-18 14:50:46
*/
@Service
public class NoticeReceiverServiceImpl extends ServiceImpl<NoticeReceiverMapper, NoticeReceiver>
    implements NoticeReceiverService {

}




