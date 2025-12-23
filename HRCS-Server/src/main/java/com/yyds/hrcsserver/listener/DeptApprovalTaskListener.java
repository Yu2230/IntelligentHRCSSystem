package com.yyds.hrcsserver.listener;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeptApprovalTaskListener implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        log.info("【部门审批任务创建】任务ID：{}，审批人：{}",
                delegateTask.getId(),
                delegateTask.getAssignee());

        // 可扩展：发送邮件/短信通知
        // notificationService.sendNotification(delegateTask.getAssignee(), "您有新的审批任务");
    }
}