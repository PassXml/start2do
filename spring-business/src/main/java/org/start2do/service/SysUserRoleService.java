package org.start2do.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.start2do.ebean.service.AbsService;
import org.start2do.entity.security.SysUserRole;


@Service
@RequiredArgsConstructor
@EnableConfigurationProperties({DataSourceProperties.class})

public class SysUserRoleService extends AbsService<SysUserRole> {

}
