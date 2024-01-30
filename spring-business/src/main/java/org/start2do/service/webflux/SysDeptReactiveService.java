package org.start2do.service.webflux;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.start2do.dto.BusinessException;
import org.start2do.ebean.service.AbsReactiveService;
import org.start2do.entity.security.SysDept;
import org.start2do.entity.security.query.QSysUser;
import reactor.core.publisher.Mono;


@Service
@RequiredArgsConstructor
@EnableConfigurationProperties({DataSourceProperties.class})
@ConditionalOnWebApplication(type = Type.REACTIVE)

public class SysDeptReactiveService extends AbsReactiveService<SysDept, Integer> {


    private final SysUserReactiveService sysUserService;

    public Mono<Boolean> remove(Integer id) {
        return sysUserService.count(new QSysUser().deptId.eq(id)).filter(integer -> integer <= 0).switchIfEmpty(
            Mono.error(new BusinessException("请先删除该节点下的用户"))
        ).then(deleteById(id));
    }
}
