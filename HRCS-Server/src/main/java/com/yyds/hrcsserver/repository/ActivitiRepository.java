package com.yyds.hrcsserver.repository;

import com.yyds.hrcspojo.entity.Apply;
import com.yyds.hrcspojo.entity.User;
import lombok.RequiredArgsConstructor;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// repository/ActivitiRepository.java
@Repository
@RequiredArgsConstructor
public class ActivitiRepository {

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final HistoryService historyService;

    /**
     * 启动请假流程
     */
    public String startLeaveProcess(Apply apply, User applicant, Long firstApproverId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("applicantId", apply.getApplicantId().toString());
        variables.put("role", applicant.getRole());
        variables.put("departmentId", applicant.getDepartmentId());
        variables.put("deptManagerId", firstApproverId != null ? firstApproverId.toString() : "");
        variables.put("adminId", getAdminId()); // 获取管理员ID

        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByKey("leaveProcess", apply.getId().toString(), variables);

        return processInstance.getId();
    }

    /**
     * 获取当前任务
     */
    public Task getCurrentTask(String processInstanceId) {
        return taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
    }

    /**
     * 完成任务
     */
    public void completeTask(String taskId, Map<String, Object> variables) {
        taskService.complete(taskId, variables);
    }

    /**
     * 查询待办任务列表
     */
    public List<Task> getTodoTasks(String userId) {
        return taskService.createTaskQuery()
                .taskAssignee(userId)
                .orderByTaskCreateTime()
                .desc()
                .list();
    }

    /**
     * 获取管理员ID
     */
    private String getAdminId() {
        // 从系统中获取管理员ID，可缓存
        return "1"; // 实际应从配置或数据库获取
    }
}