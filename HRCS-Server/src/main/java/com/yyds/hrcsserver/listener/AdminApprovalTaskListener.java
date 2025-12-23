package com.yyds.hrcsserver.listener;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AdminApprovalTaskListener implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        log.info("【管理员审批任务创建】任务ID：{}，审批人：{}",
                delegateTask.getId(),
                delegateTask.getAssignee());
    }
}