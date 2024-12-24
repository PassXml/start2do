package org.start2do.service.servlet;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.start2do.dto.BusinessException;
import org.start2do.ebean.service.AbsService;
import org.start2do.entity.security.SysDept;
import org.start2do.entity.security.query.QSysUser;


@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "start2do.business.service", name = "dept", havingValue = "true")
public class SysDeptService extends AbsService<SysDept> {

    private final SysUserService sysUserService;

    public void remove(Integer id) {
        if (sysUserService.count(new QSysUser().deptId.eq(id)) > 0) {
            throw new BusinessException("请先删除该节点下的用户");
        }
        deleteById(id);
    }
}
