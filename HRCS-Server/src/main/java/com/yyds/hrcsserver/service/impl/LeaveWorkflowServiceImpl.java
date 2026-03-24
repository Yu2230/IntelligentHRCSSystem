package com.yyds.hrcsserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yyds.hrcscommon.result.PageResult;
import com.yyds.hrcscommon.result.Result;
import com.yyds.hrcspojo.apply.ApplyDTO;
import com.yyds.hrcspojo.apply.ApplyPageQueryDTO;
import com.yyds.hrcspojo.apply.ApplyVO;
import com.yyds.hrcspojo.entity.Apply;
import com.yyds.hrcspojo.entity.Department;
import com.yyds.hrcspojo.entity.User;
import com.yyds.hrcsserver.repository.ApplyRepository;
import com.yyds.hrcsserver.repository.DepartmentRepository;

import com.yyds.hrcsserver.repository.UserRepository;
import com.yyds.hrcsserver.service.LeaveWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.HistoryServiceImpl;
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
    private final HistoryService historyService;

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
        Long deptId = applicant.getDepartmentId();
        if (deptId != null) {
            variables.put("departmentId", deptId.toString());
        }
        variables.put("adminId", getAdminId().toString());

        if (applicant.getRole() == 2 && deptId != null) {
            Long deptManagerId = getDeptManagerId(deptId);
            variables.put("deptManagerId", deptManagerId.toString());
        }

        ProcessInstance instance = runtimeService.startProcessInstanceByKey(
                "leaveApprovalProcess",
                apply.getId().toString(),
                variables
        );

        apply.setProcessInstanceId(instance.getId());
        apply.setDepartmentId(applicant.getDepartmentId());
        // 冗余记录当前审批人，便于展示（即便流程未正确分配assignee）
        Long initialApproverId;
        if (applicant.getRole() == 2 && applicant.getDepartmentId() != null) {
            initialApproverId = getDeptManagerId(applicant.getDepartmentId());
        } else {
            initialApproverId = getAdminId();
        }
        apply.setCurrentApproverId(initialApproverId);
        applyRepository.updateById(apply);

        return Result.getSuccessResult("申请已提交");
    }

    @Override
    public Result approve(Long applyId, Long approverId, Integer action, String comment) {
        Apply apply = applyRepository.getById(applyId);
        if (apply == null) {
            return Result.getErrorResultByMsg("申请不存在");
        }
        if (!StringUtils.hasText(apply.getProcessInstanceId())) {
            return Result.getErrorResultByMsg("流程数据缺失，请联系管理员或重新提交申请");
        }

        Task task = taskService.createTaskQuery()
                .processInstanceId(apply.getProcessInstanceId())
                .singleResult();

        if (task == null) {
            return Result.getErrorResultByMsg("任务已处理或流程已结束");
        }

        String assignee = task.getAssignee();
        if (!StringUtils.hasText(assignee)) {
            return Result.getErrorResultByMsg("当前任务未分配审批人，请联系管理员");
        }
        if (!assignee.equals(approverId.toString())) {
            return Result.getErrorResultByMsg("无权操作");
        }

        User approver = userRepository.getById(approverId);
        if (approver == null) {
            return Result.getErrorResultByMsg("审批人不存在");
        }
        String taskDefinitionKey = task.getTaskDefinitionKey();

        // ===== 审批顺序控制（不依赖historyService）=====
        if ("deptManagerTask".equals(taskDefinitionKey)) {
            if (!Integer.valueOf(3).equals(approver.getRole())) {
                return Result.getErrorResultByMsg("当前需要部门负责人审批");
            }
        } else if ("adminTask".equals(taskDefinitionKey)) {
            if (!Integer.valueOf(1).equals(approver.getRole())) {
                return Result.getErrorResultByMsg("当前需要管理员审批");
            }

            // 使用运行时服务验证部门审批是否通过
            // 获取流程实例ID
            String processInstanceId = apply.getProcessInstanceId();

            // 查询deptApproved变量（如果变量不存在或false，说明部门未审批）
            Object deptApprovedObj = runtimeService.getVariable(processInstanceId, "deptApproved");
            if (deptApprovedObj == null || !(Boolean) deptApprovedObj) {
                return Result.getErrorResultByMsg("部门负责人尚未审批通过");
            }
        } else {
            return Result.getErrorResultByMsg("未知的审批节点: " + taskDefinitionKey);
        }
        // ============================================

//        Map<String, Object> variables = new HashMap<>();
//        if (approver.getRole() == 3) {
//            variables.put("deptApproved", action == 1);
//        } else if (approver.getRole() == 1) {
//            variables.put("adminApproved", action == 1);
//        }
//
//        taskService.complete(task.getId(), variables);
//
//        // 更新业务状态
//        ProcessInstance pi = runtimeService.createProcessInstanceQuery()
//                .processInstanceId(apply.getProcessInstanceId())
//                .singleResult();
//
//        if (pi == null) {
//            // 流程结束，查询最终审批结果
//            // 使用runtimeService获取最终变量
//            Object adminApprovedObj = runtimeService.getVariable(
//                    apply.getProcessInstanceId(), "adminApproved");
//            boolean finalApproved = adminApprovedObj != null && (Boolean) adminApprovedObj;
//
//            apply.setState(finalApproved ? 1 : 2);
//            apply.setCurrentApproverId(null);
//            apply.setUpdateTime(new Date());
//            applyRepository.updateById(apply);
//        } else {
//            // 流程未结束，更新当前审批人（冗余字段）
//            Task nextTask = taskService.createTaskQuery()
//                    .processInstanceId(apply.getProcessInstanceId())
//                    .singleResult();
//            Long nextApproverId = resolveApproverIdFromTask(apply, nextTask);
//            if (nextApproverId != null) {
//                apply.setCurrentApproverId(nextApproverId);
//                apply.setUpdateTime(new Date());
//                applyRepository.updateById(apply);
//            }
//        }
        Map<String, Object> variables = new HashMap<>();
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
            // ========== 流程结束，使用 historyService 查询历史变量 ==========

            // 方案1：查询历史变量（推荐）
            HistoricVariableInstance historicVar = historyService
                    .createHistoricVariableInstanceQuery()
                    .processInstanceId(apply.getProcessInstanceId())
                    .variableName("adminApproved")
                    .singleResult();

            boolean finalApproved = historicVar != null &&
                    Boolean.TRUE.equals(historicVar.getValue());

            // 或者方案2：直接根据 action 判断（更简洁）
            // boolean finalApproved = (action == 1);

            apply.setState(finalApproved ? 1 : 2);
            apply.setCurrentApproverId(null);
            apply.setUpdateTime(new Date());
            applyRepository.updateById(apply);
        } else {
            // 流程未结束，更新当前审批人（冗余字段）
            Task nextTask = taskService.createTaskQuery()
                    .processInstanceId(apply.getProcessInstanceId())
                    .singleResult();
            Long nextApproverId = resolveApproverIdFromTask(apply, nextTask);
            if (nextApproverId != null) {
                apply.setCurrentApproverId(nextApproverId);
                apply.setUpdateTime(new Date());
                applyRepository.updateById(apply);
            }
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
        if (apply.getState() == 0) {
            Task task = null;
            if (StringUtils.hasText(apply.getProcessInstanceId())) {
                task = taskService.createTaskQuery()
                        .processInstanceId(apply.getProcessInstanceId())
                        .singleResult();
            }
            Long approverId = resolveApproverIdFromTask(apply, task);
            if (approverId == null && apply.getCurrentApproverId() != null) {
                approverId = apply.getCurrentApproverId();
            }
            if (approverId == null && apply.getDepartmentId() == null) {
                approverId = getAdminId();
            }
            if (approverId != null) {
                vo.setCurrentApproverId(approverId);
                User approver = userRepository.getById(approverId);
                vo.setCurrentApproverName(approver != null ? approver.getName() : "未知");
                vo.setCurrentApproverRole(approver != null ? approver.getRole() : null);
            } else {
                vo.setCurrentApproverName("待分配");
            }
            if (task != null) {
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

    private Long resolveApproverIdFromTask(Apply apply, Task task) {
        if (task == null) {
            return null;
        }
        String assignee = task.getAssignee();
        if (StringUtils.hasText(assignee)) {
            return parseLong(assignee);
        }
        String taskDefinitionKey = task.getTaskDefinitionKey();
        // 尝试从流程变量中解析
        if (StringUtils.hasText(apply.getProcessInstanceId())) {
            if ("deptManagerTask".equals(taskDefinitionKey)) {
                Object deptManagerIdObj = runtimeService.getVariable(apply.getProcessInstanceId(), "deptManagerId");
                Long deptManagerId = parseLong(deptManagerIdObj);
                if (deptManagerId != null) {
                    return deptManagerId;
                }
                if (apply.getDepartmentId() != null) {
                    return getDeptManagerId(apply.getDepartmentId());
                }
                return getAdminId();
            } else if ("adminTask".equals(taskDefinitionKey)) {
                Object adminIdObj = runtimeService.getVariable(apply.getProcessInstanceId(), "adminId");
                Long adminId = parseLong(adminIdObj);
                return adminId != null ? adminId : getAdminId();
            }
        }
        return null;
    }

    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.valueOf(value.toString());
        } catch (Exception ignored) {
            return null;
        }
    }
}