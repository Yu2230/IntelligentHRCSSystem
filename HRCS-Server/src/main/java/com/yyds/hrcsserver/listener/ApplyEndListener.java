package com.yyds.hrcsserver.listener;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ApplyEndListener implements ExecutionListener {

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        String processInstanceId = execution.getProcessInstanceId();
        String businessKey = execution.getBusinessKey();

        // ✅ 正确获取 Boolean 类型变量
        Boolean deptApproved = (Boolean) execution.getVariable("deptApproved");
        Boolean adminApproved = (Boolean) execution.getVariable("adminApproved");

        // 转换为 String 显示（避免空指针）
        String deptApprovedStr = deptApproved != null ? deptApproved.toString() : "N/A";
        String adminApprovedStr = adminApproved != null ? adminApproved.toString() : "N/A";

        log.info("【请假流程结束】流程实例ID：{}，业务Key：{}，部门审批：{}，管理员审批：{}",
                processInstanceId, businessKey, deptApprovedStr, adminApprovedStr);

        // 可扩展：更新业务表最终状态
        // Long applyId = Long.valueOf(businessKey);
        // boolean finalApproved = adminApproved != null && adminApproved;
        // applyRepository.updateState(applyId, finalApproved ? 1 : 2);
    }
}