package com.yyds.hrcsserver.repository;

import camundajar.impl.scala.App;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yyds.hrcscommon.result.PageResult;
import com.yyds.hrcspojo.data.user.apply.ApplyVO;
import com.yyds.hrcspojo.entity.Apply;

import java.util.List;


public interface ApplyRepository extends IService<Apply> {

    Apply getApplyByProcessInstanceId(String processInstanceId);


    List<Apply> getOwnApply(Long userId);
}