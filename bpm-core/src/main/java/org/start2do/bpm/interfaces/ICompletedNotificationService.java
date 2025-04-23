package org.start2do.bpm.interfaces;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.springframework.stereotype.Service;

@Service
public interface ICompletedNotificationService {

    String getNotificationTaskId(DelegateTask delegateTask);

    void notify(String id, DelegateTask delegateTask);
}
