package com.yyds.hrcsserver.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yyds.hrcspojo.data.user.department.DepartmentVO;
import com.yyds.hrcspojo.entity.Department;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author 21641
* @description 针对表【department(部门表)】的数据库操作Mapper
* @createDate 2025-12-16 14:05:24
* @Entity com.yyds.hrcspojo.entity.Department
*/
public interface DepartmentMapper extends BaseMapper<Department> {

    IPage<DepartmentVO> selectPageWithManager(Page<DepartmentVO> page, String name);
}




