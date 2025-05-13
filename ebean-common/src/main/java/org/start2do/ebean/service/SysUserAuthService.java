package org.start2do.ebean.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.start2do.entity.security.SysUserAuth;

@Service
@ConditionalOnProperty(
    prefix = "start2do.ebean",
    name = {"enable", "enable-user-auth-service"},
    havingValue = "true",
    matchIfMissing = true)
public class SysUserAuthService extends AbsService<SysUserAuth> {
    // AbsService<SysUserAuth> 将处理基于 String 类型 ID 的 CRUD 操作
}
