package org.start2do.bpm.listener;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;
import org.start2do.bpm.dto.Constant;

@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalExecutionListener implements ExecutionListener {

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        Map<String, Object> map = execution.getVariables();
        if (ExecutionListener.EVENTNAME_START.equals(execution.getEventName())) {
            log.info("{},{},{}", execution.getProcessDefinitionId(), execution.getCurrentActivityName(), map);
        }
        if (ExecutionListener.EVENTNAME_END.equals(execution.getEventName())) {
            log.info("{},{},{}", execution.getProcessDefinitionId(), execution.getCurrentActivityName(), map);
            map.remove(Constant.AGREE);
            map.remove(Constant.MSG);
        } else {
            log.info("事件,{}", execution.getEventName());
        }

    }
}
