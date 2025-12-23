package com.yyds.hrcsserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yyds.hrcscommon.result.PageResult;
import com.yyds.hrcscommon.result.Result;
import com.yyds.hrcspojo.data.user.apply.ApplyDTO;
import com.yyds.hrcspojo.data.user.apply.ApplyPageQueryDTO;
import com.yyds.hrcspojo.data.user.apply.ApplyVO;
import com.yyds.hrcspojo.entity.Apply;
import com.yyds.hrcspojo.entity.Department;
import com.yyds.hrcspojo.entity.User;
import com.yyds.hrcsserver.repository.ApplyRepository;
import com.yyds.hrcsserver.repository.DepartmentRepository;

import com.yyds.hrcsserver.repository.UserRepository;
import com.yyds.hrcsserver.service.LeaveWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LeaveWorkflowServiceImpl implements LeaveWorkflowService {

    private final ApplyRepository applyRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository deptRepository;
    private final RuntimeService runtimeService;
    private final TaskService taskService;

    // ==================== 提交与审批 ====================

    @Override
    public Result submitApply(ApplyDTO dto) {
        User applicant = userRepository.getById(dto.getApplicantId());
        if (applicant == null) {
            return Result.getErrorResultByMsg("用户不存在");
        }

        Apply apply = new Apply();
        BeanUtils.copyProperties(dto, apply);
        apply.setState(0);
        apply.setCreateTime(new Date());
        apply.setUpdateTime(new Date());
        applyRepository.save(apply);

        // 启动流程
        Map<String, Object> variables = new HashMap<>();
        variables.put("applicantId", dto.getApplicantId().toString());
        variables.put("applicantRole", applicant.getRole());
        variables.put("departmentId", applicant.getDepartmentId().toString());
        variables.put("adminId", getAdminId().toString());

        if (applicant.getRole() == 2 && applicant.getDepartmentId() != null) {
            Long deptManagerId = getDeptManagerId(applicant.getDepartmentId());
            variables.put("deptManagerId", deptManagerId.toString());
        }

        ProcessInstance instance = runtimeService.startProcessInstanceByKey(
                "leaveApprovalProcess",
                apply.getId().toString(),
                variables
        );

        apply.setProcessInstanceId(instance.getId());
        apply.setDepartmentId(applicant.getDepartmentId());
        applyRepository.updateById(apply);

        return Result.getSuccessResult("申请已提交");
    }

    @Override
    public Result approve(Long applyId, Long approverId, Integer action, String comment) {
        Apply apply = applyRepository.getById(applyId);
        if (apply == null || StringUtils.isEmpty(apply.getProcessInstanceId())) {
            return Result.getErrorResultByMsg("申请不存在");
        }

        Task task = taskService.createTaskQuery()
                .processInstanceId(apply.getProcessInstanceId())
                .singleResult();

        if (task == null || !task.getAssignee().equals(approverId.toString())) {
            return Result.getErrorResultByMsg("无权操作或任务已处理");
        }

        Map<String, Object> variables = new HashMap<>();
        User approver = userRepository.getById(approverId);

        if (approver.getRole() == 3) {
            variables.put("deptApproved", action == 1);
        } else if (approver.getRole() == 1) {
            variables.put("adminApproved", action == 1);
        }

        taskService.complete(task.getId(), variables);

        // 更新业务状态
        ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                .processInstanceId(apply.getProcessInstanceId())
                .singleResult();

        if (pi == null) {
            apply.setState(action == 1 ? 1 : 2);
            apply.setUpdateTime(new Date());
            applyRepository.updateById(apply);
        }

        return Result.getSuccessResult(action == 1 ? "审批通过" : "已驳回");
    }

    // ==================== 查询功能 ====================

    @Override
    public PageResult<ApplyVO> getApplyPage(ApplyPageQueryDTO dto) {
        Page<Apply> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<Apply> wrapper = buildQueryWrapper(dto);

        IPage<Apply> applyPage = applyRepository.page(page, wrapper);
        List<ApplyVO> voList = convertToVOList(applyPage.getRecords());

        return PageResult.build(voList, applyPage.getTotal(), dto.getPageNum(), dto.getPageSize());
    }

    @Override
    public PageResult<ApplyVO> getDeptApplyPage(ApplyPageQueryDTO dto) {
        if (dto.getDepartmentId() == null) {
            throw new IllegalArgumentException("部门ID不能为空");
        }
        return getApplyPage(dto);
    }

    @Override
    public IPage<ApplyVO> getOwnApply(Long userId, Integer pageNum, Integer pageSize) {
        Page<Apply> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Apply> wrapper = new LambdaQueryWrapper<Apply>()
                .eq(Apply::getApplicantId, userId)
                .orderByDesc(Apply::getCreateTime);

        IPage<Apply> applyPage = applyRepository.page(page, wrapper);
        return applyPage.convert(this::convertToVO);
    }

    @Override
    public List<ApplyVO> getTodoList(Long userId) {
        // 查询用户作为审批人的任务
        List<Task> tasks = taskService.createTaskQuery()
                .taskAssignee(userId.toString())
                .list();

        if (tasks.isEmpty()) {
            return Collections.emptyList();
        }

        // 获取关联的申请ID
        List<Long> applyIds = tasks.stream()
                .map(task -> {
                    String businessKey = runtimeService.createProcessInstanceQuery()
                            .processInstanceId(task.getProcessInstanceId())
                            .singleResult()
                            .getBusinessKey();
                    return Long.valueOf(businessKey);
                })
                .collect(Collectors.toList());

        // 查询申请详情
        List<Apply> applies = applyRepository.listByIds(applyIds);
        return convertToVOList(applies);
    }

    // ==================== 辅助方法 ====================

    private LambdaQueryWrapper<Apply> buildQueryWrapper(ApplyPageQueryDTO dto) {
        LambdaQueryWrapper<Apply> wrapper = new LambdaQueryWrapper<>();

        if (dto.getApplicantId() != null) {
            wrapper.eq(Apply::getApplicantId, dto.getApplicantId());
        }
        if (dto.getDepartmentId() != null) {
            wrapper.eq(Apply::getDepartmentId, dto.getDepartmentId());
        }
        if (dto.getState() != null) {
            wrapper.eq(Apply::getState, dto.getState());
        }
        if (dto.getStartDate() != null) {
            wrapper.ge(Apply::getStartTime, dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            wrapper.le(Apply::getEndTime, dto.getEndDate());
        }

        wrapper.orderByDesc(Apply::getCreateTime);
        return wrapper;
    }

    private List<ApplyVO> convertToVOList(List<Apply> applies) {
        return applies.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    private ApplyVO convertToVO(Apply apply) {
        ApplyVO vo = new ApplyVO();
        BeanUtils.copyProperties(apply, vo);

        // 补充申请人信息
        User applicant = userRepository.getById(apply.getApplicantId());
        if (applicant != null) {
            vo.setApplicantName(applicant.getName());
            vo.setApplicantRole(applicant.getRole());
            vo.setDepartmentId(applicant.getDepartmentId());

            // 补充部门信息
            if (applicant.getDepartmentId() != null) {
                Department dept = deptRepository.getById(applicant.getDepartmentId());
                vo.setDepartmentName(dept != null ? dept.getDepartmentName() : "未知部门");
            }
        }

        // 补充状态文本
        vo.setStateText(getStateText(apply.getState()));

        // 补充当前审批人信息
        if (apply.getState() == 0 && StringUtils.hasText(apply.getProcessInstanceId())) {
            Task task = taskService.createTaskQuery()
                    .processInstanceId(apply.getProcessInstanceId())
                    .singleResult();
            if (task != null) {
                vo.setCurrentApproverId(Long.valueOf(task.getAssignee()));
                User approver = userRepository.getById(vo.getCurrentApproverId());
                vo.setCurrentApproverName(approver != null ? approver.getName() : "未知");
                vo.setCurrentApproverRole(approver != null ? approver.getRole() : null);
                vo.setCurrentTaskName(task.getName());
            }
        }

        return vo;
    }

    private String getStateText(Integer state) {
        switch (state) {
            case 0: return "审批中";
            case 1: return "已通过";
            case 2: return "已驳回";
            default: return "未知";
        }
    }

    private Long getDeptManagerId(Long deptId) {
        Department dept = deptRepository.getById(deptId);
        return (dept != null && dept.getManagerId() != null) ? dept.getManagerId() : getAdminId();
    }

    private Long getAdminId() {
        // 从数据库查询管理员ID
        User admin = userRepository.getOne(
                new LambdaQueryWrapper<User>().eq(User::getRole, 1).last("LIMIT 1")
        );
        return admin != null ? admin.getId() : 1L;
    }
}