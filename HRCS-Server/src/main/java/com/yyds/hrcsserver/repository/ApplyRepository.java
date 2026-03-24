package com.yyds.hrcsserver.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yyds.hrcspojo.entity.Apply;

import java.util.List;


public interface ApplyRepository extends IService<Apply> {

    Apply getApplyByProcessInstanceId(String processInstanceId);


    List<Apply> getOwnApply(Long userId);
}