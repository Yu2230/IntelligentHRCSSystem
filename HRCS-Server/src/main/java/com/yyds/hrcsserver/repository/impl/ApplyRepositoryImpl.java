package com.yyds.hrcsserver.repository.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyds.hrcspojo.entity.Apply;
import com.yyds.hrcsserver.mapper.ApplyMapper;
import com.yyds.hrcsserver.repository.ApplyRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ApplyRepositoryImpl
        extends ServiceImpl<ApplyMapper, Apply>
            implements ApplyRepository {
    @Override
    public Apply getApplyByProcessInstanceId(String processInstanceId) {
        return lambdaQuery()
                .eq(Apply::getProcessInstanceId, processInstanceId)
                .oneOpt()
                .orElse(null);
    }

    @Override
    public List<Apply> getOwnApply(Long userId) {
        return lambdaQuery()
                .eq(Apply::getApplicantId, userId)
                .list();
    }


}
