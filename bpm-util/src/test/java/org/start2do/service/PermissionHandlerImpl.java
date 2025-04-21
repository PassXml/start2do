package org.start2do.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.warm.flow.core.handler.PermissionHandler;
import org.springframework.stereotype.Component;
import org.start2do.bpm.service.IUserHandle;

@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionHandlerImpl implements PermissionHandler {

    private final IUserHandle iUserHandle;

    @Override
    public List<String> permissions() {
        // 办理人权限标识，比如用户，角色，部门等, 流程设计时未设置办理人或者ignore为true可不传 [按需传输]

        return List.of();
    }

    @Override
    public String getHandler() {
        return iUserHandle.getCurrentUsername();
    }
}
