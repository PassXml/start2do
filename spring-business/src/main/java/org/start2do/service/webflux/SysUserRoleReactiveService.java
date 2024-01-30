package org.start2do.service.webflux;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.start2do.dto.BusinessException;
import org.start2do.ebean.service.AbsReactiveService;
import org.start2do.entity.security.SysUserRole;
import org.start2do.entity.security.query.QSysUserRole;
import org.start2do.util.ListUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
@RequiredArgsConstructor
@EnableConfigurationProperties({DataSourceProperties.class})

@ConditionalOnWebApplication(type = Type.REACTIVE)
public class SysUserRoleReactiveService extends AbsReactiveService<SysUserRole, Integer> {

    public Mono<Boolean> save(Integer roleId, List<Integer> userId) {
        List<Integer> integers = userId.stream().filter(Objects::nonNull).toList();
        return findAll(new QSysUserRole().roleId.eq(roleId)).flatMap(roles -> {
            List<Mono<Boolean>> result = new ArrayList<>();
            ListUtil.diff(integers, roles, (integer, sysUserRole) -> integer.equals(sysUserRole.getUserId()), add -> {
                for (Integer integer : add) {
                    result.add(save(new SysUserRole(integer, roleId)).map(sysUserRole -> true));
                }
            }, eqValues -> {

            }, dels -> {
                if (dels.isEmpty()) {
                    return;
                }
                result.add(delete(new QSysUserRole().roleId.eq(roleId).userId.in(
                    dels.stream().map(SysUserRole::getUserId).toList())));
            });
            return Flux.fromIterable(result).flatMap(Function.identity()).all(Boolean::booleanValue);
        }).filter(aBoolean -> aBoolean).switchIfEmpty(Mono.error(new BusinessException("保存失败")));
    }
}
