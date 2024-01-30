package org.start2do.service.servlet;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.start2do.ebean.service.AbsService;
import org.start2do.entity.security.SysUserRole;
import org.start2do.entity.security.query.QSysUserRole;
import org.start2do.util.ListUtil;


@Service
@RequiredArgsConstructor
@EnableConfigurationProperties({DataSourceProperties.class})
@ConditionalOnWebApplication(type = Type.SERVLET)
public class SysUserRoleService extends AbsService<SysUserRole> {

    @Transactional(rollbackFor = Exception.class)
    public void save(Integer roleId, List<Integer> userId) {
        List<Integer> integers = userId.stream().filter(Objects::nonNull).collect(Collectors.toList());
        List<SysUserRole> roles = findAll(new QSysUserRole().roleId.eq(roleId));
        ListUtil.diff(integers, roles, (integer, sysUserRole) -> integer.equals(sysUserRole.getUserId()), add -> {
            for (Integer integer : add) {
                save(new SysUserRole(integer, roleId));
            }
        }, eqValues -> {

        }, dels -> {
            if (dels.isEmpty()) {
                return;
            }
            delete(new QSysUserRole().roleId.eq(roleId).userId.in(
                dels.stream().map(SysUserRole::getUserId).collect(Collectors.toList())));
        });
    }
}
