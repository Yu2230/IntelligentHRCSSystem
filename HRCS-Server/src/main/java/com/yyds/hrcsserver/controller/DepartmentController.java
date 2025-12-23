package com.yyds.hrcsserver.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yyds.hrcscommon.result.Result;
import com.yyds.hrcspojo.data.user.department.DepartmentVO;
import com.yyds.hrcspojo.data.user.department.DepartmentWithUsersDTOS;
import com.yyds.hrcspojo.data.user.department.DepartmentWithUsersVO;
import com.yyds.hrcspojo.entity.Department;
import com.yyds.hrcsserver.service.DepartmentService;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "部门模块", description = "部门相关接口")  // ✅ 替换 @Api
@RestController
@RequestMapping("/department")
public class DepartmentController {
    private final DepartmentService departmentService;
    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    /**
     * 创建部门
     */
    @RequestMapping("/create")
    @ApiOperation(value = "创建部门", notes = "创建部门")
    public Result create(@RequestBody Department department) {
        boolean result = departmentService.createDepartmentWithManager(department);
        return result ? Result.getSuccessResultByMsg("创建部门成功") : Result.getErrorResultByMsg("创建部门失败");
    }


    // 1. 移除部门成员
    @DeleteMapping("/{deptId}/users")
    @ApiOperation(value = "移除部门成员", notes = "移除部门成员")
    public Result removeUsers(@PathVariable Long deptId, @RequestBody List<Long> userIds) {
        boolean success = departmentService.removeUsersFromDepartment(deptId, userIds);
        return success ? Result.getSuccessResultByMsg("移除部门成员成功") : Result.getErrorResultByMsg("移除部门成员失败");
    }

    @DeleteMapping("/{deptId}")
    @ApiOperation(value = "强制删除部门", notes = "强制删除部门")
    public Result deleteDept(@PathVariable Long deptId) {
        boolean success = departmentService.deleteWithUsers(deptId);
        return success ? Result.getSuccessResultByMsg("删除部门成功") : Result.getErrorResultByMsg("删除部门失败");
    }

    // 3. 强制删除部门（连同管理员）
    @DeleteMapping("/{deptId}/force")
    @ApiOperation(value = "强制删除部门", notes = "强制删除部门")
    public Result forceDeleteDept(@PathVariable Long deptId) {
        boolean success = departmentService.deleteDepartment(deptId, true);
        return success ? Result.getSuccessResultByMsg("删除部门成功") : Result.getErrorResultByMsg("删除部门失败");
    }
    /**
     * 修改部门
     */
    @RequestMapping("/update")
    @ApiOperation(value = "修改部门", notes = "修改部门")
    public Result update(@RequestBody Department department) {

        boolean result = departmentService.updateDepartmentWithManager(department);
        return result ? Result.getSuccessResultByMsg("修改部门成功") : Result.getErrorResultByMsg("修改部门失败");
    }
    @GetMapping("/page")
    @ApiOperation(value = "分页查询部门", notes = "分页查询部门")
    public Result<IPage<DepartmentVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name) {
        IPage<DepartmentVO> pageData = departmentService.pageQuery(pageNum, pageSize, name);
        return Result.getSuccessResult(pageData);
    }

    /**
     * 获得部门人员详情
     */
    @GetMapping("/getDepartmentWithUsers")
    @ApiOperation(value = "获得部门人员详情", notes = "获得部门人员详情")
    public Result<DepartmentWithUsersVO> getDepartmentWithUsers(@RequestParam("id") Integer id) {
        DepartmentWithUsersVO departmentWithUsersVO = departmentService.getDepartmentWithUsers(id);
        return Result.getSuccessResult(departmentWithUsersVO);
    }


    /**
     * 添加部门成员
     */
    @PostMapping("/addUser")
    @ApiOperation(value = "添加部门成员", notes = "添加部门成员")
    public Result addUser(@RequestBody DepartmentWithUsersDTOS departmentWithUsersDTOS) {
        boolean result = departmentService.addUser(departmentWithUsersDTOS);
        return result ? Result.getSuccessResultByMsg("添加部门成员成功") : Result.getErrorResultByMsg("添加部门成员失败");
    }

    /**
     * 统计部门员工数量
     */
    @GetMapping("/countUsers")
    @ApiOperation(value = "统计部门员工数量", notes = "统计部门员工数量")
    public Result<Integer> countUsers(@RequestParam("id") Long id) {
        Integer count = departmentService.countUsers(id);
        return Result.getSuccessResult(count);
    }

}
