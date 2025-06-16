package org.start2do.cep.action;

import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IActionMap {

    public static IActionMap iActionMap;
    @Getter
    private final List<IAction> actions;

    @PostConstruct
    public void init() {
        IActionMap.iActionMap = this;

    }
}
