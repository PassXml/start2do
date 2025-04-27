package org.start2do.ebean.config;

import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.start2do.ebean.util.EntityHook;
import org.start2do.ebean.util.EntityHookUtil;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class EntityHookAutoConfig {

    private final List<EntityHook> entityHooks;

    @PostConstruct
    public void init() {
        for (EntityHook hook : entityHooks) {
            log.info("添加EntityHook:{}", hook.getKey());
            EntityHookUtil.addHooks(hook);
        }
    }
}
