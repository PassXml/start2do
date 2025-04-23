package org.start2do.bpm.test;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.springframework.stereotype.Service;
import org.start2do.bpm.interfaces.ICompletedNotificationService;

@Slf4j
@Service
public class CompletedNotificationServiceImpl implements ICompletedNotificationService {

    @Override
    public String getNotificationTaskId(DelegateTask delegateTask) {
        return null;
    }

    @Override
    public void notify(String id, DelegateTask delegateTask) {
        log.info("id,{}", id);
    }
}
