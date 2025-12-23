package com.yyds.hrcsserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyds.hrcspojo.entity.Apply;


import com.yyds.hrcsserver.mapper.ApplyMapper;
import com.yyds.hrcsserver.service.ApplyService;
import org.springframework.stereotype.Service;

/**
* @author 21641
* @description 针对表【apply(请假申请表)】的数据库操作Service实现
* @createDate 2025-12-19 16:02:56
*/
@Service
public class ApplyServiceImpl extends ServiceImpl<ApplyMapper, Apply>
    implements ApplyService {

}




