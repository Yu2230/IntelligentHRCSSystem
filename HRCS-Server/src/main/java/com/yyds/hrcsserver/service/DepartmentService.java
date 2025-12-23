package com.yyds.hrcsserver.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yyds.hrcspojo.data.user.department.DepartmentVO;
import com.yyds.hrcspojo.data.user.department.DepartmentWithUsersDTOS;
import com.yyds.hrcspojo.data.user.department.DepartmentWithUsersVO;
import com.yyds.hrcspojo.entity.Department;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 21641
* @description 针对表【department(部门表)】的数据库操作Service
* @createDate 2025-12-16 14:05:24
*/
public interface DepartmentService extends IService<Department> {


    boolean updateDepartmentWithManager(Department department);

    boolean createDepartmentWithManager(Department department);

    IPage<DepartmentVO> pageQuery(Integer pageNum, Integer pageSize, String name);

    boolean deleteWithUsers(Long id);

    DepartmentWithUsersVO getDepartmentWithUsers(Integer id);

    boolean addUser(DepartmentWithUsersDTOS departmentWithUsersDTOS);

    Integer countUsers(Long id);

    boolean removeUsersFromDepartment(Long deptId, List<Long> userIds);

    boolean deleteDepartment(Long deptId, boolean b);
}
