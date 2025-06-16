package org.start2do.service;

import java.util.ArrayList;
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

    /**
     * 即使用户有多个 dept: 开头的权限，系统也只是把它们看作是列表里两个完全独立的字符串。它会逐一检查用户的所有权限，看其中是否有任何一个能和任务要求的权限字符串完全对上。
     */
    @Override
    public List<String> permissions() {
        // 办理人权限标识，比如用户，角色，部门等, 流程设计时未设置办理人或者ignore为true可不传 [按需传输]
        List<String> result = new ArrayList<>();
        result.add("userId:" + iUserHandle.getCurrentUserId());
        result.addAll(iUserHandle.getDeptCodes().stream().map(t -> "dept:" + t).toList());
        result.addAll(iUserHandle.getPositionCodes().stream().map(t -> "position:" + t).toList());
        return result;
    }

    @Override
    public String getHandler() {
        return iUserHandle.getCurrentUsername();
    }
}
