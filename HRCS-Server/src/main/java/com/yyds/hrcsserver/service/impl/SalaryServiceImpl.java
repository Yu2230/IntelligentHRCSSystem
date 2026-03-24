package com.yyds.hrcsserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.yyds.hrcscommon.client.MailClient;
import com.yyds.hrcscommon.result.PageResult;

import com.yyds.hrcscommon.result.Result;
import com.yyds.hrcspojo.salary.MonthlyStatVO;
import com.yyds.hrcspojo.salary.SalaryDetailDTO;
import com.yyds.hrcspojo.salary.SalaryQueryDTO;
import com.yyds.hrcspojo.salary.SalaryVO;
import com.yyds.hrcspojo.entity.*;


import com.yyds.hrcsserver.mapper.*;
import com.yyds.hrcsserver.service.SalaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SalaryServiceImpl extends ServiceImpl<SalaryMapper, Salary> implements SalaryService {
    private static final BigDecimal DEFAULT_BASE_SALARY = new BigDecimal("5000.00");
    private static final BigDecimal LATE_PENALTY = new BigDecimal("50");
    private static final BigDecimal ABSENT_PENALTY = new BigDecimal("200");
    private static final BigDecimal WORK_DAYS = new BigDecimal("22");
    private final SalaryMapper salaryMapper;
    private final SalaryDetailMapper salaryDetailMapper;
    private final UserSalaryProfileMapper userSalaryProfileMapper;
    private final UserMapper userMapper;
    private final DepartmentMapper departmentMapper;
    private final AttendanceRecordMapper attendanceRecordMapper;
    private final ApplyMapper applyMapper;
    private final MailClient mailClient;

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
        BigDecimal baseSalary = profile != null && profile.getBaseSalary() != null ? profile.getBaseSalary() : DEFAULT_BASE_SALARY;

        Salary salary = new Salary();
        salary.setUserId(emp.getId());
        salary.setUserName(emp.getName());
        salary.setDeptId(emp.getDepartmentId());
        salary.setMonth(month);
        salary.setBaseSalary(baseSalary);
        salary.setTotalCommission(BigDecimal.ZERO);
        salary.setActualSalary(baseSalary);
        salary.setStatus(1);
        salary.setIssuerId(issuerId);
        salary.setIssueTime(new Date());
        salary.setCreateBy(issuerId);
        salary.setUpdateBy(issuerId);
        salaryMapper.insert(salary);
        // 计算请假与考勤扣款
        List<SalaryDetail> details = new ArrayList<>();
        details.addAll(buildLeaveDeductionDetails(emp, salary.getId(), baseSalary, month, issuerId));
        details.addAll(buildAttendanceDeductionDetails(emp, salary.getId(), month, issuerId));

        if (!details.isEmpty()) {
            for (SalaryDetail detail : details) {
                salaryDetailMapper.insert(detail);
            }
        }

        BigDecimal totalCommission = calcTotalCommission(details);
        salary.setTotalCommission(totalCommission);
        salary.setActualSalary(baseSalary.add(totalCommission));
        salary.setUpdateBy(issuerId);
        salary.setUpdateTime(new Date());
        salaryMapper.updateById(salary);

        sendSalaryNotice(emp, salary, details);
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

    /**
     * 生成请假扣款明细（仅统计当月已审批通过的请假）
     */
    private List<SalaryDetail> buildLeaveDeductionDetails(User user, Long salaryId, BigDecimal baseSalary, String month, Long issuerId) {
        List<SalaryDetail> details = new ArrayList<>();
        YearMonth yearMonth = YearMonth.parse(month);
        Date startDate = Date.from(yearMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(yearMonth.atEndOfMonth().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<Apply> applies = applyMapper.selectList(
                new LambdaQueryWrapper<Apply>()
                        .eq(Apply::getApplicantId, user.getId())
                        .eq(Apply::getState, 1)
                        .le(Apply::getStartTime, endDate)
                        .ge(Apply::getEndTime, startDate)
        );
        if (applies == null || applies.isEmpty()) {
            return details;
        }

        int totalDays = applies.stream()
                .mapToInt(apply -> {
                    if (apply.getDays() != null) {
                        return apply.getDays();
                    }
                    if (apply.getStartTime() != null && apply.getEndTime() != null) {
                        LocalDate start = apply.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        LocalDate end = apply.getEndTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        long days = ChronoUnit.DAYS.between(start, end) + 1;
                        return (int) Math.max(days, 0);
                    }
                    return 0;
                })
                .sum();
        if (totalDays <= 0) {
            return details;
        }

        BigDecimal dailySalary = baseSalary.divide(WORK_DAYS, 2, RoundingMode.HALF_UP);
        BigDecimal amount = dailySalary.multiply(new BigDecimal(totalDays)).setScale(2, RoundingMode.HALF_UP);

        SalaryDetail detail = new SalaryDetail();
        detail.setSalaryId(salaryId);
        detail.setCommissionType(1);
        detail.setCommissionName("请假扣款");
        detail.setCommissionAmount(amount);
        detail.setDescription("请假" + totalDays + "天，日薪" + dailySalary);
        detail.setCreateBy(issuerId);
        detail.setCreateTime(new Date());
        details.add(detail);
        return details;
    }

    /**
     * 生成考勤扣款明细（迟到>30分钟、缺勤）
     */
    private List<SalaryDetail> buildAttendanceDeductionDetails(User user, Long salaryId, String month, Long issuerId) {
        List<SalaryDetail> details = new ArrayList<>();
        YearMonth yearMonth = YearMonth.parse(month);
        Date startDate = Date.from(yearMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(yearMonth.atEndOfMonth().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<AttendanceRecord> records = attendanceRecordMapper.selectList(
                new LambdaQueryWrapper<AttendanceRecord>()
                        .eq(AttendanceRecord::getUserId, user.getId())
                        .ge(AttendanceRecord::getWorkDate, startDate)
                        .lt(AttendanceRecord::getWorkDate, endDate)
        );
        if (records == null || records.isEmpty()) {
            return details;
        }
        long lateCount = records.stream().filter(r -> r.getIsLate() != null && r.getIsLate() == 1).count();
        long absentCount = records.stream().filter(r -> r.getIsAbsent() != null && r.getIsAbsent() == 1).count();

        if (lateCount > 0) {
            SalaryDetail late = new SalaryDetail();
            late.setSalaryId(salaryId);
            late.setCommissionType(1);
            late.setCommissionName("迟到扣款");
            late.setCommissionAmount(LATE_PENALTY.multiply(new BigDecimal(lateCount)));
            late.setDescription("迟到次数：" + lateCount + "次（超过30分钟）");
            late.setCreateBy(issuerId);
            late.setCreateTime(new Date());
            details.add(late);
        }
        if (absentCount > 0) {
            SalaryDetail absent = new SalaryDetail();
            absent.setSalaryId(salaryId);
            absent.setCommissionType(1);
            absent.setCommissionName("缺勤扣款");
            absent.setCommissionAmount(ABSENT_PENALTY.multiply(new BigDecimal(absentCount)));
            absent.setDescription("缺勤次数：" + absentCount + "次");
            absent.setCreateBy(issuerId);
            absent.setCreateTime(new Date());
            details.add(absent);
        }
        return details;
    }

    private BigDecimal calcTotalCommission(List<SalaryDetail> details) {
        if (details == null || details.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return details.stream()
                .map(d -> d.getCommissionType() == 0 ? d.getCommissionAmount() : d.getCommissionAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 工资发放邮件通知
     */
    private void sendSalaryNotice(User user, Salary salary, List<SalaryDetail> details) {
        if (user == null || user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return;
        }
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("您好，").append(user.getName() == null ? user.getUserName() : user.getName()).append("，\n");
            builder.append("您的 ").append(salary.getMonth()).append(" 工资已发放。\n");
            builder.append("基础工资：").append(salary.getBaseSalary()).append("\n");
            if (details != null && !details.isEmpty()) {
                builder.append("调整明细：\n");
                for (SalaryDetail detail : details) {
                    builder.append("- ").append(detail.getCommissionName())
                            .append(": ").append(detail.getCommissionAmount());
                    if (detail.getDescription() != null) {
                        builder.append("（").append(detail.getDescription()).append("）");
                    }
                    builder.append("\n");
                }
            }
            builder.append("实发工资：").append(salary.getActualSalary()).append("\n");
            builder.append("如有疑问，请联系管理员。\n");

            mailClient.sendCustomMail(user.getEmail(), "工资发放通知", builder.toString());
        } catch (Exception e) {
            log.warn("工资通知邮件发送失败 userId={}, msg={}", user.getId(), e.getMessage());
        }
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