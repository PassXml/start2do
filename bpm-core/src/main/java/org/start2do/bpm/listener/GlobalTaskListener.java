package org.start2do.bpm.listener;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.springframework.stereotype.Component;
import org.start2do.bpm.interfaces.ICompletedNotificationService;
import org.start2do.script.dto.ScriptRunnerResult;
import org.start2do.script.util.ScriptRunner;

@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalTaskListener implements TaskListener {

    private final ICompletedNotificationService iCompletedNotificationService;

    @Override
    public void notify(DelegateTask task) {
        log.info("GlobalTaskListener:{}", task.getName());
        eventnameAssignment(task);
        if (TaskListener.EVENTNAME_COMPLETE.equals(task.getEventName())) {
            String id = iCompletedNotificationService.getNotificationTaskId(task);
            log.debug("流程:{},任务 '{}' (ID: {}) 已完成，触发传阅通知监听器。", task.getProcessDefinitionId(),
                task.getName(), task.getId());
            iCompletedNotificationService.notify(id, task);
        }
    }

    private void eventnameAssignment(DelegateTask task) {
        if (TaskListener.EVENTNAME_ASSIGNMENT.equals(task.getEventName())) {
            String assignee = task.getAssignee();
            if (StringUtils.isNotEmpty(assignee)) {
                if (assignee.startsWith("#{script:")) {
                    String scriptId = assignee.substring(9, assignee.length() - 1);
                    ScriptRunnerResult result = ScriptRunner.evalById(scriptId, "taskName", task.getName(), "vars",
                        task.getVariables());
                    if (result.isSuccess()) {
                        Object object = result.getResult();
                        if (object instanceof String) {
                            task.setAssignee(object.toString());
                        }
                        if (object instanceof List) {
                            List<String> o = (List<String>) object;
                            task.setAssignee(null);
                            task.addCandidateUsers(o);
                        }
                    }
                }
            }
        }
    }
}
