package com.yyds.hrcsserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.yyds.hrcscommon.result.PageResult;

import com.yyds.hrcscommon.result.Result;
import com.yyds.hrcspojo.data.user.salary.MonthlyStatVO;
import com.yyds.hrcspojo.data.user.salary.SalaryDetailDTO;
import com.yyds.hrcspojo.data.user.salary.SalaryQueryDTO;
import com.yyds.hrcspojo.data.user.salary.SalaryVO;
import com.yyds.hrcspojo.entity.*;


import com.yyds.hrcsserver.mapper.*;
import com.yyds.hrcsserver.service.SalaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SalaryServiceImpl extends ServiceImpl<SalaryMapper, Salary> implements SalaryService {
    private final SalaryMapper salaryMapper;
    private final SalaryDetailMapper salaryDetailMapper;
    private final UserSalaryProfileMapper userSalaryProfileMapper;
    private final UserMapper userMapper;
    private final DepartmentMapper departmentMapper;

    @Override
    public PageResult<SalaryVO> getSalaryPage(SalaryQueryDTO dto) {
        Page<Salary> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<Salary> wrapper = new LambdaQueryWrapper<Salary>()
                .eq(dto.getUserId() != null, Salary::getUserId, dto.getUserId())
                .eq(dto.getDeptId() != null, Salary::getDeptId, dto.getDeptId())
                .eq(StringUtils.isNotBlank(dto.getMonth()), Salary::getMonth, dto.getMonth())
                .eq(dto.getStatus() != null, Salary::getStatus, dto.getStatus())
                .like(StringUtils.isNotBlank(dto.getUserName()), Salary::getUserName, dto.getUserName())
                .orderByDesc(Salary::getMonth, Salary::getUpdateTime);

        IPage<Salary> result = salaryMapper.selectPage(page, wrapper);
        List<SalaryVO> voList = result.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        return PageResult.build(voList, result.getTotal(), dto.getPageNum(), dto.getPageSize());
    }

    @Override
    public SalaryVO getUserSalary(Long userId, String month) {
        Salary salary = salaryMapper.selectOne(new LambdaQueryWrapper<Salary>().eq(Salary::getUserId, userId).eq(Salary::getMonth, month));
        if (salary == null) return null;
        List<SalaryDetail> details = salaryDetailMapper.selectList(new LambdaQueryWrapper<SalaryDetail>().eq(SalaryDetail::getSalaryId, salary.getId()));
        SalaryVO vo = convertToVO(salary);
        vo.setDetailList(details);
        return vo;
    }

    @Override
    public Result<String> setBaseSalary(Long userId, BigDecimal baseSalary, Long operatorId) {
        User user = userMapper.selectById(userId);
        if (user == null) return Result.getErrorResultByMsg("用户不存在");

        Integer role = user.getRole();
        if (role == null) return Result.getErrorResultByMsg("用户角色信息异常");

        // 只允许员工（2）和部门负责人（3）
        if (role != 2 && role != 3) return Result.getErrorResultByMsg("管理员用户无需设置基础工资");

        UserSalaryProfile profile = userSalaryProfileMapper.selectOne(
                new LambdaQueryWrapper<UserSalaryProfile>().eq(UserSalaryProfile::getUserId, userId)
        );

        if (profile != null) {
            profile.setBaseSalary(baseSalary);
            profile.setEffectiveDate(new Date());
            profile.setUpdateBy(operatorId);
            profile.setUpdateTime(new Date());
            userSalaryProfileMapper.updateById(profile);
        } else {
            UserSalaryProfile newProfile = new UserSalaryProfile();
            newProfile.setUserId(userId);
            newProfile.setBaseSalary(baseSalary);
            newProfile.setEffectiveDate(new Date());
            newProfile.setCreateBy(operatorId);
            newProfile.setUpdateBy(operatorId);
            userSalaryProfileMapper.insert(newProfile);
        }

        log.info("【基础工资设置】用户{}基础工资设置为：{}", userId, baseSalary);
        return Result.getSuccessResult("基础工资设置成功");
    }

    @XxlJob("issueMonthlySalaryJob")
    public void issueMonthlySalaryJob(String param) {
        log.info("【XXL-JOB】执行月度工资发放任务，参数：{}", param);
        Long issuerId = StringUtils.isNotBlank(param) ? Long.valueOf(param) : 1L;
        try {
            issueMonthlySalary(issuerId);
            long count = getIssueCount();
            XxlJobHelper.log("发放成功，共{}人", count);
            XxlJobHelper.handleSuccess("发放成功：" + count + "人");
        } catch (Exception e) {
            log.error("发放失败", e);
            XxlJobHelper.log("失败：" + e.getMessage());
            XxlJobHelper.handleFail(e.getMessage());
        }
    }

    @Override
    public void issueMonthlySalary(Long issuerId) {
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        log.info("【薪酬发放】开始发放{}月工资", currentMonth);

        List<User> employees = userMapper.selectList(new LambdaQueryWrapper<User>().ne(User::getRole, 1));
        for (User emp : employees) {
            try {
                issueEmployeeSalary(emp, currentMonth, issuerId);
            } catch (Exception e) {
                log.error("员工{}发放失败：{}", emp.getId(), e.getMessage());
            }
        }
    }

    private void issueEmployeeSalary(User emp, String month, Long issuerId) {
        Salary existing = salaryMapper.selectOne(new LambdaQueryWrapper<Salary>().eq(Salary::getUserId, emp.getId()).eq(Salary::getMonth, month));
        if (existing != null) {
            log.warn("员工{}工资已存在，跳过", emp.getId());
            return;
        }

        // 从薪酬档案表获取基础工资
        UserSalaryProfile profile = userSalaryProfileMapper.selectOne(
                new LambdaQueryWrapper<UserSalaryProfile>().eq(UserSalaryProfile::getUserId, emp.getId())
        );
        BigDecimal baseSalary = profile != null ? profile.getBaseSalary() : new BigDecimal("5000.00");

        // 计算调整项
        List<SalaryDetail> details = salaryDetailMapper.selectList(
                new LambdaQueryWrapper<SalaryDetail>().eq(SalaryDetail::getSalaryId, existing != null ? existing.getId() : null)
        );
        BigDecimal totalCommission = BigDecimal.ZERO;
        // 实际应从commission_rule表获取，此处简化

        Salary salary = new Salary();
        salary.setUserId(emp.getId());
        salary.setUserName(emp.getName());
        salary.setDeptId(emp.getDepartmentId());
        salary.setMonth(month);
        salary.setBaseSalary(baseSalary);
        salary.setTotalCommission(totalCommission);
        salary.setActualSalary(baseSalary.add(totalCommission));
        salary.setStatus(1);
        salary.setIssuerId(issuerId);
        salary.setIssueTime(new Date());
        salary.setCreateBy(issuerId);
        salary.setUpdateBy(issuerId);
        salaryMapper.insert(salary);

        log.info("员工{}工资发放成功，实发：{}", emp.getId(), salary.getActualSalary());
    }

    @Override
    public void addCommission(SalaryDetailDTO dto) {
        Salary salary = salaryMapper.selectById(dto.getSalaryId());
        if (salary == null) throw new RuntimeException("工资记录不存在");

        SalaryDetail detail = new SalaryDetail();
        BeanUtils.copyProperties(dto, detail);
        detail.setCreateBy(dto.getOperatorId());
        detail.setCreateTime(new Date());
        salaryDetailMapper.insert(detail);

        recalculateSalary(salary.getId());
    }

    private void recalculateSalary(Long salaryId) {
        Salary salary = salaryMapper.selectById(salaryId);
        List<SalaryDetail> details = salaryDetailMapper.selectList(
                new LambdaQueryWrapper<SalaryDetail>().eq(SalaryDetail::getSalaryId, salaryId)
        );

        BigDecimal totalCommission = details.stream()
                .map(d -> d.getCommissionType() == 0 ? d.getCommissionAmount() : d.getCommissionAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        salary.setTotalCommission(totalCommission);
        salary.setActualSalary(salary.getBaseSalary().add(totalCommission));
        salary.setUpdateTime(new Date());
        salaryMapper.updateById(salary);
    }

    @Override
    public List<MonthlyStatVO> getMonthlyStats(String year) {
        return salaryMapper.selectMonthlyStats(year);
    }

    private SalaryVO convertToVO(Salary salary) {
        SalaryVO vo = new SalaryVO();
        BeanUtils.copyProperties(salary, vo);
        if (salary.getDeptId() != null) {
            Department dept = departmentMapper.selectById(salary.getDeptId());
            vo.setDeptName(dept != null ? dept.getDepartmentName() : "未知部门");
        }
        vo.setStatusText(salary.getStatus() != null && salary.getStatus() == 1 ? "已发放" : "未发放");
        return vo;
    }

    private long getIssueCount() {
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        return salaryMapper.selectCount(new LambdaQueryWrapper<Salary>().eq(Salary::getMonth, currentMonth));
    }
}