package com.yyds.hrcsserver.service;

import com.yyds.hrcscommon.result.PageResult;
import com.yyds.hrcscommon.result.Result;
import com.yyds.hrcspojo.data.user.salary.MonthlyStatVO;
import com.yyds.hrcspojo.data.user.salary.SalaryDetailDTO;
import com.yyds.hrcspojo.data.user.salary.SalaryQueryDTO;
import com.yyds.hrcspojo.data.user.salary.SalaryVO;
import com.yyds.hrcspojo.entity.Salary;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;

/**
* @author 21641
* @description 针对表【salary(工资表)】的数据库操作Service
* @createDate 2025-12-20 15:45:53
*/
public interface SalaryService extends IService<Salary> {
    // 分页查询薪资
    PageResult<SalaryVO> getSalaryPage(SalaryQueryDTO dto);

    // 查询用户某月工资详情
    SalaryVO getUserSalary(Long userId, String month);

    Result<String> setBaseSalary(Long userId, BigDecimal baseSalary, Long operatorId);

    // 手动发放工资（XXL-JOB调用）
    void issueMonthlySalary(Long issuerId);

    // 添加奖金/扣款
    void addCommission(SalaryDetailDTO dto);

    // 月度统计
    List<MonthlyStatVO> getMonthlyStats(String year);
}
