package com.yyds.hrcsserver.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import com.yyds.hrcspojo.entity.User;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserRepository extends IService<User> {
    Optional<User> selectOptByEmail(String email);



    //Optional<User> selectOptById(Long id);

    Optional<User> selectOptByUserName(String userName); // Changed from Optional<User> to Optional<List<User>>

    Optional<List<User>> selectOptByPhone(String phone);

    Optional<List<User>> selectOptByBankCard(String bankCard);

    Optional<List<User>> selectOptByIdCard(String idCard);

    Optional<User> selectOptByName(String name);

     List<User> selectAllUser(Integer pageNum, Integer pageSize, String name);

    boolean existsByPhone(String phone);

    boolean existsByUserName(String userName);

    boolean existsByIdCard(String idCard);

    boolean existsByBankCard(String bankCard);

    User selectForUpdate(@NotBlank(message = "用户ID不能为空") Long id);

    List<User> getAnyUser();

    Collection<? extends User> getUserByDepartmentId(Integer id);

    List<User> selectByDepartmentId(Long id);

    Integer countByDepartmentId(Long id);

    List<User> selectByDeptIds(List<Long> deptIds);

    void removeUserDepartment(Long userId);

    Map<Long, Long> countByJobPositionIds(List<Long> jobPositionIds);


    List<User> selectList(LambdaQueryWrapper<User> eq);

    int getUserCount();

    int getUserCountForCurrentMonth();

    List<Map<String, Object>> getDailyUserCount(LocalDate startDate, LocalDate today);

    Long getAdminId();

    /**
     * 员工搜索（姓名/用户名/邮箱/手机号/工号）
     */
    List<User> searchEmployees(String keyword, Integer limit);
}
