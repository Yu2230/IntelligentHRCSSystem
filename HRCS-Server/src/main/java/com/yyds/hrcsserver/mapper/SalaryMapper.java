package com.yyds.hrcsserver.mapper;

import com.yyds.hrcspojo.salary.MonthlyStatVO;
import com.yyds.hrcspojo.entity.Salary;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author 21641
* @description 针对表【salary(工资表)】的数据库操作Mapper
* @createDate 2025-12-20 15:45:53
* @Entity com.yyds.hrcspojo.entity.Salary
*/
public interface SalaryMapper extends BaseMapper<Salary> {

    List<MonthlyStatVO> selectMonthlyStats(String year);

}




