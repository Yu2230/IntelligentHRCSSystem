package com.yyds.hrcsserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyds.hrcspojo.entity.CommissionRule;

import com.yyds.hrcsserver.mapper.CommissionRuleMapper;
import com.yyds.hrcsserver.service.CommissionRuleService;
import org.springframework.stereotype.Service;

/**
* @author 21641
* @description 针对表【commission_rule(工资调整规则表)】的数据库操作Service实现
* @createDate 2025-12-20 15:46:01
*/
@Service
public class CommissionRuleServiceImpl extends ServiceImpl<CommissionRuleMapper, CommissionRule>
    implements CommissionRuleService {

}




