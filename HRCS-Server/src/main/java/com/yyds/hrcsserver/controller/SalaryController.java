package com.yyds.hrcsserver.controller;// controller/SalaryController.java


import com.yyds.hrcscommon.result.PageResult;
import com.yyds.hrcscommon.result.Result;

import com.yyds.hrcspojo.data.user.salary.MonthlyStatVO;
import com.yyds.hrcspojo.data.user.salary.SalaryDetailDTO;
import com.yyds.hrcspojo.data.user.salary.SalaryQueryDTO;
import com.yyds.hrcspojo.data.user.salary.SalaryVO;
import com.yyds.hrcsserver.service.SalaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/salary")
@RequiredArgsConstructor
public class SalaryController {
    private final SalaryService salaryService;

    /** 1. 分页查询薪资列表（支持姓名模糊） */
    @GetMapping("/page")
    public Result<PageResult<SalaryVO>> getSalaryPage(SalaryQueryDTO dto) {
        return Result.getSuccessResult(salaryService.getSalaryPage(dto));
    }

    /** 2. 查询用户某月工资详情 */
    @GetMapping("/user/{userId}")
    public Result<SalaryVO> getUserSalary(@PathVariable Long userId, @RequestParam String month) {
        return Result.getSuccessResult(salaryService.getUserSalary(userId, month));
    }

    /** 3. 设置基础工资（仅限员工和负责人） */
    @PostMapping("/profile")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> setBaseSalary(@RequestParam Long userId,
                                        @RequestParam BigDecimal baseSalary,
                                        @RequestParam Long operatorId) {
        return salaryService.setBaseSalary(userId, baseSalary, operatorId);
    }

    /** 4. 手动触发工资发放（管理员） */
    @PostMapping("/issue")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> issueSalary(@RequestParam Long issuerId) {
        salaryService.issueMonthlySalary(issuerId);
        return Result.getSuccessResult("工资发放任务已启动");
    }

    /** 5. 添加奖金/扣款（管理员） */
    @PostMapping("/commission")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> addCommission(@RequestBody SalaryDetailDTO dto) {
        salaryService.addCommission(dto);
        return Result.getSuccessResult("调整已添加");
    }

    /** 6. 获取年度月度统计（管理员） */
    @GetMapping("/monthly-stats/{year}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<MonthlyStatVO>> getMonthlyStats(@PathVariable String year) {
        return Result.getSuccessResult(salaryService.getMonthlyStats(year));
    }

    /** 7. 获取用户薪资历史（分页） */
    @GetMapping("/history/{userId}")
    public Result<PageResult<SalaryVO>> getUserHistory(@PathVariable Long userId,
                                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                                       @RequestParam(defaultValue = "10") Integer pageSize) {
        SalaryQueryDTO dto = new SalaryQueryDTO();
        dto.setUserId(userId);
        dto.setPageNum(pageNum);
        dto.setPageSize(pageSize);
        return Result.getSuccessResult(salaryService.getSalaryPage(dto));
    }
}