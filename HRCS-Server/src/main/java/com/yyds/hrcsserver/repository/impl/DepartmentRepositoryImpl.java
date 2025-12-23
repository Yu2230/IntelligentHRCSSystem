package com.yyds.hrcsserver.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyds.hrcspojo.entity.Department;


import com.yyds.hrcsserver.mapper.DepartmentMapper;
import com.yyds.hrcsserver.repository.DepartmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;


@Slf4j
@Repository
public class DepartmentRepositoryImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentRepository {
    @Override
    public Department selectForUpdate(Long id) {
        return lambdaQuery()
                .eq(Department::getId, id)
                .last("FOR UPDATE")
                .one();
    }

    @Override
    public List<Department> pageQuery(Integer pageNum, Integer pageSize, String name) {
        return lambdaQuery()
                .like(StringUtils.isNotBlank(name), Department::getDepartmentName, name)  // 模糊查询
                .orderByDesc(Department::getCreateTime)
                .page(new Page<>(pageNum, pageSize))
                .getRecords();
    }

    @Override
    public Department selectByManagerId(Long managerId) {
        return lambdaQuery()
                .eq(Department::getManagerId, managerId)
                .one();
    }

    @Override
    public boolean deleteById(Long id) {
        return removeById(id);
    }

}
