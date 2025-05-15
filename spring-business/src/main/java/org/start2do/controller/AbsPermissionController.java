package org.start2do.controller;

import java.util.Set;
import org.start2do.dto.permission.PermissionDto;

/**
 * 权限控制器接口
 */
public interface AbsPermissionController {

    /**
     * 获取所有URL映射
     *
     * @return URL权限信息集合
     */
    Set<PermissionDto> getAllUrls();
}
