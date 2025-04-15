package org.start2do.service;

import lombok.extern.slf4j.Slf4j;
import org.dromara.warm.flow.core.listener.GlobalListener;
import org.dromara.warm.flow.core.listener.ListenerVariable;
import org.springframework.stereotype.Service;

/**
 * 全局监听器
 */
@Slf4j
@Service
public class GlobalListenerImpl implements GlobalListener {

    @Override
    public void start(ListenerVariable listenerVariable) {
        log.info("流程开始:{},变量:{}", listenerVariable.getInstance().getId(), listenerVariable.getVariable());
    }

    @Override
    public void assignment(ListenerVariable listenerVariable) {
        log.info("分派监听器,{},{}", listenerVariable.getInstance().getId(), listenerVariable.getVariable());
    }

    @Override
    public void finish(ListenerVariable listenerVariable) {
        log.info("完成了");
    }

    @Override
    public void create(ListenerVariable listenerVariable) {
        log.info("创建");
    }

    @Override
    public void notify(String type, ListenerVariable listenerVariable) {
        log.info("通知");
    }
}
