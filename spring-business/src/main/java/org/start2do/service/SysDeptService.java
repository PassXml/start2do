package org.start2do.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.start2do.dto.BusinessException;
import org.start2do.ebean.service.AbsService;
import org.start2do.entity.security.SysDept;
import org.start2do.entity.security.query.QSysUser;


@Service
@RequiredArgsConstructor
@EnableConfigurationProperties({DataSourceProperties.class})

public class SysDeptService extends AbsService<SysDept> {

    private final SysUserService sysUserService;

    public void remove(Integer id) {
        if (sysUserService.count(new QSysUser().deptId.eq(id)) > 0) {
            throw new BusinessException("请先删除该节点下的用户");
        }
        deleteById(id);
    }
}
