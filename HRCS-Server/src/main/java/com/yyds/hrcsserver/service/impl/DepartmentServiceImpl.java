package com.yyds.hrcsserver.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyds.hrcscommon.constants.ErrorEnum;
import com.yyds.hrcscommon.constants.PostConstants;
import com.yyds.hrcscommon.exception.BusinessException;
import com.yyds.hrcspojo.data.user.department.DepartmentVO;
import com.yyds.hrcspojo.data.user.department.DepartmentWithUsersDTOS;
import com.yyds.hrcspojo.data.user.department.DepartmentWithUsersVO;
import com.yyds.hrcspojo.entity.Department;


import com.yyds.hrcspojo.entity.User;
import com.yyds.hrcsserver.mapper.DepartmentMapper;
import com.yyds.hrcsserver.repository.DepartmentRepository;
import com.yyds.hrcsserver.repository.UserRepository;
import com.yyds.hrcsserver.service.DepartmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author 21641
* @description 针对表【department(部门表)】的数据库操作Service实现
* @createDate 2025-12-16 14:05:24
*/
@Service
@Slf4j
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department>
    implements DepartmentService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;
    public DepartmentServiceImpl(UserRepository userRepository,
                                 DepartmentRepository departmentRepository, DepartmentMapper departmentMapper) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.departmentMapper = departmentMapper;
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDepartmentWithManager(Department department) {
        log.info("【更新部门】开始，部门ID: {}, 新主管ID: {}", department.getId(), department.getManagerId());

        // 1. 参数校验
        if (department.getId() == null) {
            throw new BusinessException(ErrorEnum.DEPT_ID_IS_NULL);
        }
        if (department.getManagerId() == null) {
            throw new BusinessException(ErrorEnum.MANAGER_ID_IS_NULL);
        }

        // 2. 查询并锁定部门（防止并发）
        Department dbDept = departmentRepository.selectForUpdate(department.getId());
        if (dbDept == null) {
            throw new BusinessException(ErrorEnum.DEPT_NOT_FOUND);
        }

        // 3. 查询并锁定用户
        User user = userRepository.selectForUpdate(department.getManagerId());
        if (user == null) {
            throw new BusinessException(ErrorEnum.USER_NOT_FOUND);
        }

        // 4. ✅ 检查用户是否已经是其他部门的负责人
        Department managedDept = departmentRepository.selectByManagerId(department.getManagerId());
        if (managedDept != null && !managedDept.getId().equals(department.getId())) {
            log.warn("【更新部门】失败，用户ID: {} 已是部门 {} 的负责人", department.getManagerId(), managedDept.getId());
            throw new BusinessException(ErrorEnum.USER_ALREADY_MANAGER);
        }

        // 5. 更新用户部门ID（解决类型转换和NPE）
        user.setDepartmentId(department.getId()); // 建议：数据库字段统一用Long
        user.setRole(PostConstants.department_admin);

        // 6. 执行更新（任一失败都会回滚）
        boolean userUpdated = userRepository.updateById(user);
        boolean deptUpdated = departmentRepository.updateById(department);

        boolean success = userUpdated && deptUpdated;
        log.info("【更新部门】结果: user更新={}, dept更新={}, 成功={}", userUpdated, deptUpdated, success);
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createDepartmentWithManager(Department department) {
        log.info("【创建部门】开始，主管ID: {}", department.getManagerId());

        // 1. 参数校验
        if (department.getManagerId() == null) {
            throw new BusinessException(ErrorEnum.MANAGER_ID_IS_NULL);
        }

        // 2. 查询用户（必须存在）
        User user = userRepository.getById(department.getManagerId());
        if (user == null) {
            throw new BusinessException(ErrorEnum.USER_NOT_FOUND);
        }

        //  检查用户是否已经是其他部门的负责人
        Department managedDept = departmentRepository.selectByManagerId(department.getManagerId());
        if (managedDept != null) {
            log.warn("【创建部门】失败，用户ID: {} 已是部门 {} 的负责人", department.getManagerId(), managedDept.getId());
            throw new BusinessException(ErrorEnum.USER_ALREADY_MANAGER);
        }

        // 4. 先保存部门（生成ID）
        boolean deptSaved = departmentRepository.save(department);
        if (!deptSaved || department.getId() == null) {
            throw new BusinessException(ErrorEnum.DEPT_CREATE_FAILED);
        }

        // 5. 再更新用户的部门ID
        user.setDepartmentId(department.getId());
        user.setRole(PostConstants.department_admin);
        boolean userUpdated = userRepository.updateById(user);

        boolean success = userUpdated;
        log.info("【创建部门】成功，部门ID: {}, 结果: {}", department.getId(), success);
        return success;
    }

    @Override
    public IPage<DepartmentVO> pageQuery(Integer pageNum, Integer pageSize, String name) {
        log.info("【部门分页查询】开始，页码: {}, 每页数量: {}, 部门名称关键字: {}", pageNum, pageSize, name);

        // 1. 先分页查询部门基本信息
        Page<DepartmentVO> page = new Page<>(pageNum, pageSize);
        IPage<DepartmentVO> departmentPage = departmentMapper.selectPageWithManager(page, name);

        // 如果没有数据，直接返回
        if (departmentPage.getRecords().isEmpty()) {
            log.info("【部门分页查询】未查询到数据");
            return departmentPage;
        }

        // 2. 提取所有部门ID，批量查询用户
        List<Long> deptIds = departmentPage.getRecords().stream()
                .map(DepartmentVO::getId)
                .collect(Collectors.toList());

        log.info("【部门分页查询】批量查询用户，部门ID列表: {}", deptIds);
        List<User> users = userRepository.selectByDeptIds(deptIds);

        // 3. 按部门ID分组用户
        Map<Long, List<User>> userMap = users.stream()
                .collect(Collectors.groupingBy(User::getDepartmentId));

        // 4. 填充每个部门的 userList
        departmentPage.getRecords().forEach(dept -> {
            List<User> deptUsers = userMap.getOrDefault(dept.getId(), Collections.emptyList());
            dept.setUserList(deptUsers);
            log.debug("【部门分页查询】部门ID: {}，填充用户数量: {}", dept.getId(), deptUsers.size());
        });

        log.info("【部门分页查询】完成，总记录数: {}", departmentPage.getTotal());
        return departmentPage;
    }
    // 简化版本：当只剩管理员时直接删除部门（包含管理员）
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteWithUsers(Long id) {
        return deleteDepartment(id, true);
    }
    //todo
    /**
     * 删除部门（当部门只剩管理员时，允许直接删除）
     * @param id 部门ID
     * @param forceDelete 是否强制删除（true: 连同管理员一起删除，false: 仅当部门为空时删除）
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDepartment(Long id, boolean forceDelete) {
        log.info("【删除部门】开始，部门ID: {}, 是否强制删除: {}", id, forceDelete);

        // 1. 参数校验
        if (id == null) {
            throw new BusinessException(ErrorEnum.DEPT_ID_IS_NULL);
        }

        // 2. 查询并锁定部门
        Department department = departmentRepository.selectForUpdate(id);
        if (department == null) {
            log.warn("【删除部门】失败，部门不存在，ID: {}", id);
            throw new BusinessException(ErrorEnum.DEPT_NOT_FOUND);
        }

        // 3. 查询部门所有成员
        List<User> departmentUsers = userRepository.selectByDepartmentId(id);

        // 4. 判断是否允许删除
        if (!departmentUsers.isEmpty()) {
            if (forceDelete) {
                // 强制删除模式：检查是否只剩管理员
                if (departmentUsers.size() == 1 &&
                        PostConstants.department_admin == (departmentUsers.get(0).getRole())) {
                    // 只剩管理员，允许删除
                    log.info("【删除部门】部门只剩管理员，将连同管理员一起删除，部门ID: {}", id);
                    // 先删除管理员
                    User manager = departmentUsers.get(0);
                    manager.setDepartmentId(null);
                    manager.setRole(PostConstants.user);
                    userRepository.removeUserDepartment(manager.getId());
                } else {
                    // 还有普通成员
                    log.warn("【删除部门】失败，部门 {} 还存在 {} 个非管理员成员，需先移除",
                            id, departmentUsers.size() - 1);
                    throw new BusinessException(ErrorEnum.DEPT_HAS_NON_MANAGER_USERS);
                }
            } else {
                // 非强制删除模式：部门必须为空
                log.warn("【删除部门】失败，部门 {} 下存在 {} 个成员，需先清空部门",
                        id, departmentUsers.size());
                throw new BusinessException(ErrorEnum.DEPT_HAS_USERS);
            }
        }

        // 5. 删除部门
        boolean deleted = departmentRepository.deleteById(id);
        log.info("【删除部门】成功，部门ID: {}, 删除结果: {}", id, deleted);
        return deleted;
    }

    @Override
    public DepartmentWithUsersVO getDepartmentWithUsers(Integer id) {
        DepartmentWithUsersVO vo = new DepartmentWithUsersVO();
        vo.setDepartment(departmentRepository.getById(id));
        vo.getUserList().addAll(userRepository.getUserByDepartmentId(id));
        return vo;
    }



    /**
     * 提取公共逻辑：设置用户部门ID（消除重复代码）
     */
    private void bindUserToDepartment(Long userId, Long departmentId) {
        if (userId == null || departmentId == null) {
            return;
        }
        User user = userRepository.selectForUpdate(userId);
        if (user == null) {
            throw new BusinessException(ErrorEnum.USER_NOT_FOUND);
        }
        user.setDepartmentId(departmentId); // 建议：统一用Long类型
        userRepository.updateById(user);
    }

    /**
     * 添加部门成员
     * @param departmentWithUsersDTOS
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addUser(DepartmentWithUsersDTOS departmentWithUsersDTOS) {
        log.info("【添加部门成员】开始，部门ID: {}, 待添加用户数: {}",
                departmentWithUsersDTOS.getDepartmentId(),
                departmentWithUsersDTOS.getUsers().size());

        // 1. 参数校验
        if (departmentWithUsersDTOS.getDepartmentId() == null) {
            throw new BusinessException(ErrorEnum.DEPT_ID_IS_NULL);
        }
        if (departmentWithUsersDTOS.getUsers() == null || departmentWithUsersDTOS.getUsers().isEmpty()) {
            throw new BusinessException(ErrorEnum.USER_LIST_IS_EMPTY);
        }

        // 2. 查询并锁定部门（防并发）
        Department department = departmentRepository.selectForUpdate(departmentWithUsersDTOS.getDepartmentId());
        if (department == null) {
            throw new BusinessException(ErrorEnum.DEPT_NOT_FOUND);
        }

        // 3. 遍历检查每个用户
        for (User user : departmentWithUsersDTOS.getUsers()) {
            if (user.getId() == null) {
                throw new BusinessException(ErrorEnum.USER_ID_IS_NULL);
            }

            // 查询并锁定用户（防并发修改）
            User dbUser = userRepository.selectForUpdate(user.getId());
            if (dbUser == null) {
                log.warn("【添加部门成员】失败，用户不存在，ID: {}", user.getId());
                throw new BusinessException(ErrorEnum.USER_NOT_FOUND);
            }

            // 核心校验：用户是否已在其他部门
            if (dbUser.getDepartmentId() != null
                    && !dbUser.getDepartmentId().equals(departmentWithUsersDTOS.getDepartmentId())) {
                log.warn("【添加部门成员】失败，用户 {} 已在部门 {}，无法重复添加到部门 {}",
                        user.getId(), dbUser.getDepartmentId(), departmentWithUsersDTOS.getDepartmentId());
                throw new BusinessException(ErrorEnum.USER_ALREADY_IN_DEPT);
            }

            // 更新用户的部门ID（如果已在当前部门则跳过）
            if (dbUser.getDepartmentId() == null) {
                dbUser.setDepartmentId(departmentWithUsersDTOS.getDepartmentId());
                boolean updated = userRepository.updateById(dbUser);
                if (!updated) {
                    log.error("【添加部门成员】更新用户部门失败，用户ID: {}", user.getId());
                    throw new BusinessException(ErrorEnum.USER_UPDATE_FAILED);
                }
            }
        }

        log.info("【添加部门成员】成功，部门ID: {}", departmentWithUsersDTOS.getDepartmentId());
        return true;
    }

    @Override
    public Integer countUsers(Long id) {
        return userRepository.countByDepartmentId(id);
    }

    /**
     * 从部门中移除成员（单个或批量）
     * @param departmentId 部门ID
     * @param userIds 要移除的用户ID列表
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeUsersFromDepartment(Long departmentId, List<Long> userIds) {
        log.info("【移除部门成员】开始，部门ID: {}, 用户ID列表: {}", departmentId, userIds);

        // 1. 参数校验
        if (departmentId == null) {
            throw new BusinessException(ErrorEnum.DEPT_ID_IS_NULL);
        }
        if (userIds == null || userIds.isEmpty()) {
            throw new BusinessException(ErrorEnum.USER_LIST_IS_EMPTY);
        }

        // 2. 查询并锁定部门
        Department department = departmentRepository.selectForUpdate(departmentId);
        if (department == null) {
            throw new BusinessException(ErrorEnum.DEPT_NOT_FOUND);
        }

        // 3. 遍历移除用户
        for (Long userId : userIds) {
            User user = userRepository.selectForUpdate(userId);
            if (user == null) {
                log.warn("【移除部门成员】用户不存在，ID: {}", userId);
                throw new BusinessException(ErrorEnum.USER_NOT_FOUND);
            }

            // 检查用户是否属于该部门
            if (!departmentId.equals(user.getDepartmentId())) {
                log.warn("【移除部门成员】用户 {} 不属于部门 {}", userId, departmentId);
                throw new BusinessException(ErrorEnum.USER_NOT_IN_DEPT);
            }

            // 清除用户的部门信息
            user.setDepartmentId(null);
            if(user.getRole().equals(PostConstants.department_admin)){
                user.setRole(PostConstants.user);// 如果角色是部门相关的，需要清除
            }

            boolean updated = userRepository.updateById(user);
            userRepository.removeUserDepartment(userId);
            if (!updated) {
                log.error("【移除部门成员】更新用户失败，用户ID: {}", userId);
                throw new BusinessException(ErrorEnum.USER_UPDATE_FAILED);
            }
        }

        log.info("【移除部门成员】成功，共移除 {} 个用户", userIds.size());
        return true;
    }

}




