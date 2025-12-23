package com.yyds.hrcsserver.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.yyds.hrcspojo.entity.User;
import com.yyds.hrcsserver.mapper.UserMapper;
import com.yyds.hrcsserver.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class UserRepositoryImpl extends ServiceImpl<UserMapper, User> implements UserRepository {

    @Override
    public Optional<User> selectOptByEmail(String email) {
        return lambdaQuery()
                .eq(User::getEmail, email)
                .oneOpt();
    }

  /*  @Override
    public Optional<User> selectOptById(Long id) {
        Assert.notNull(id, "用户ID不能为空");  // ✅ 使用 Spring 断言

        log.debug("查询用户: id={}", id);

        return lambdaQuery()
                .eq(User::getId, id)
                .oneOpt()
                .or(() -> {
                    log.warn("用户不存在: id={}", id);
                    return Optional.empty();
                });
    }*/

    @Override
    public Optional<User> selectOptByUserName(String userName) {
        return lambdaQuery()
                .eq(User::getUserName, userName)
                .oneOpt();
    }

    @Override
    public Optional<List<User>> selectOptByPhone(String phone) {
        return Optional.ofNullable(lambdaQuery()
                .eq(User::getPhone, phone)
                .list());
    }

    @Override
    public Optional<List<User>> selectOptByBankCard(String bankCard) {

        return Optional.ofNullable(lambdaQuery()
                .eq(User::getBankCard, bankCard)
                .list());
    }

    @Override
    public Optional<List<User>> selectOptByIdCard(String idCard) {
        return Optional.ofNullable(lambdaQuery()
                .eq(User::getIdCard, idCard)
                .list());
    }

    @Override
    public Optional<User> selectOptByName(String name) {
        return lambdaQuery()
                .eq(User::getName, name)
                .oneOpt();
    }

    @Override
    public List<User> selectAllUser(Integer pageNum, Integer pageSize, String name) {
        return lambdaQuery()
                .like(StringUtils.isNotBlank(name), User::getName, name)  // 模糊查询
                .orderByDesc(User::getCreateTime)
                .page(new Page<>(pageNum, pageSize))
                .getRecords();
    }

    @Override
    public boolean existsByPhone(String phone) {
        return lambdaQuery()
                .eq(User::getPhone, phone)
                .count() > 0;
    }

    @Override
    public boolean existsByUserName(String userName) {
        return lambdaQuery()
                .eq(User::getUserName, userName)
                .count() > 0;
    }

    @Override
    public boolean existsByIdCard(String idCard) {
        return lambdaQuery()
                .eq(User::getIdCard, idCard)
                .count() > 0;
    }

    @Override
    public boolean existsByBankCard(String bankCard) {
        return lambdaQuery()
                .eq(User::getBankCard, bankCard)
                .count() > 0;
    }

    @Override
    public User selectForUpdate(Long id) {
        return lambdaQuery()
                .eq(User::getId, id)
                .last("FOR UPDATE")
                .one();
    }

    @Override
    public List<User> getAnyUser() {
        //所有用户
        return lambdaQuery()
                .list();
    }

    @Override
    public Collection<? extends User> getUserByDepartmentId(Integer id) {
        return lambdaQuery()
                .eq(User::getDepartmentId, id)
                .list();
    }

    @Override
    public List<User> selectByDepartmentId(Long id) {
        return lambdaQuery()
                .eq(User::getDepartmentId, id)
                .list();
    }

    @Override
    public Integer countByDepartmentId(Long id) {
        return Math.toIntExact(lambdaQuery()
                .eq(User::getDepartmentId, id)
                .count());
    }

    @Override
    public List<User> selectByDeptIds(List<Long> deptIds) {
        return lambdaQuery()
                .in(User::getDepartmentId, deptIds)
                .list();
    }

    @Override
    public void removeUserDepartment(Long userId) {
        baseMapper.removeUserDepartment(userId);
    }

    @Override
    public List<User> selectList(LambdaQueryWrapper<User> eq) {
        return baseMapper.selectList(eq);
    }

    @Override
    public int getUserCount() {
        return lambdaQuery()
                .count()
                .intValue();
    }
    @Override
    public int getUserCountForCurrentMonth() {
        // 获取本月第一天 00:00:00
        LocalDateTime startOfMonth = LocalDateTime.now()
                .withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        // 获取本月最后一天 23:59:59
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);

        return lambdaQuery()
                .ge(User::getCreateTime, startOfMonth)
                .le(User::getCreateTime, endOfMonth)
                .count()
                .intValue();
    }

    @Override
    public List<Map<String, Object>> getDailyUserCount(LocalDate startDate, LocalDate endDate) {
        // 1. 构建完整的日期时间范围（包含当天最后一秒）
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        return getBaseMapper().selectMaps(
                Wrappers.<User>query()  // ✅ 修正为 User 实体
                        .ge("create_time", startDateTime)
                        .le("create_time", endDateTime)
                        .select("DATE(create_time) as date", "COUNT(*) as count")
                        .groupBy("DATE(create_time)")
                        .orderByAsc("DATE(create_time)")
        );
    }

    @Override
    public Long getAdminId() {
        return lambdaQuery()
                .eq(User::getRole, 1)
                .oneOpt()
                .map(User::getId)
                .orElse(null);
    }


}



