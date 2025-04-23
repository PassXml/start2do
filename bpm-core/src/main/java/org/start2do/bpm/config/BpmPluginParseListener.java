package org.start2do.bpm.config;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.springframework.context.annotation.Configuration;
import org.start2do.bpm.listener.GlobalExecutionListener;
import org.start2do.bpm.listener.GlobalTaskListener;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BpmPluginParseListener extends AbstractBpmnParseListener {

    private final GlobalTaskListener globalTaskListener;
    private final GlobalExecutionListener executionListener;

    @Override
    public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {
        activity.addListener(ExecutionListener.EVENTNAME_START, executionListener);
        UserTaskActivityBehavior behavior = (UserTaskActivityBehavior) activity.getActivityBehavior();
        TaskDefinition taskDefinition = behavior.getTaskDefinition();
        taskDefinition.addTaskListener(TaskListener.EVENTNAME_CREATE, globalTaskListener);
    }

}
