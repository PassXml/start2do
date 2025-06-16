package org.start2do.cep.action;

import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class IActionMap {

    public static IActionMap iActionMap;
    @Getter
    private final List<IAction> actions;

    @PostConstruct
    public void init() {
        IActionMap.iActionMap = this;
        log.info("自动注册 IAction 实现类:");
        if (actions == null || actions.isEmpty()) {
            log.warn("未找到任何 IAction 实现类。");
        } else {
            actions.forEach(action -> log.info("  - 已注册: {}", action.getClass().getName()));
        }
    }
}
