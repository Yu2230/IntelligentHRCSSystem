package com.yyds.hrcsserver.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yyds.hrcspojo.entity.Department;

import java.util.List;

public interface DepartmentRepository extends IService<Department> {

    Department selectForUpdate(Long id);

    List<Department> pageQuery(Integer pageNum, Integer pageSize, String name);

    Department selectByManagerId(Long managerId);

    boolean deleteById(Long id);

    // IPage<DepartmentVO> selectPageWithManager(Page<DepartmentVO> page, String name);
}
