package org.start2do.controller.webflux;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.dto.IdStrReq;
import org.start2do.dto.Page;
import org.start2do.dto.R;
import org.start2do.dto.mapper.SysLoginLogDtoMapper;
import org.start2do.dto.req.log.SysLoginLogReq;
import org.start2do.dto.resp.log.SysLogPageResp;
import org.start2do.ebean.util.Where;
import org.start2do.entity.security.query.QSysLoginLog;
import org.start2do.service.ILoginLogOwner;
import org.start2do.service.webflux.SysLoginLogReactiveService;
import reactor.core.publisher.Mono;
import org.start2do.dto.Permission;

/**
 * 登陆日志
 */
@Slf4j
@RestController
@RequestMapping("sys/login/log")
@RequiredArgsConstructor
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnProperty(prefix = "start2do.business.controller", name = "login-log", havingValue = "true",matchIfMissing = true)
@Permission(groupName = "登录日志管理")
public class SysLoginLogController {

    private final SysLoginLogReactiveService service;
    private final ILoginLogOwner iLoginLogOwner;

    /**
     * 分页
     */
    @GetMapping("page")
    public Mono<R<Page<SysLogPageResp>>> page(SysLoginLogReq req) {
        QSysLoginLog qClass = new QSysLoginLog().createTime.geIfPresent(req.getStartTime()).createTime.ltIfPresent(
            req.getEndTime()).owner.inOrEmpty(iLoginLogOwner.getOwners());
        Where.ready().like(req.getUsername(), qClass.username).like(req.getIp(), qClass.ip);
        return service.pageReactive(qClass, req, SysLoginLogDtoMapper.INSTANCE::toSysLogPageResp).map(R::ok);
    }

    /**
     * 详情
     */
    @GetMapping("detail")
    public Mono<R<SysLogPageResp>> detail(@Valid IdStrReq req) {
        return service.getOneReactive(new QSysLoginLog().id.eq(req.getId()).owner.inOrEmpty(iLoginLogOwner.getOwners()))
            .map(SysLoginLogDtoMapper.INSTANCE::toSysLogPageResp).map(R::ok);
    }

    /**
     * 散场
     */
    @GetMapping("delete")
    public Mono<R> delete(@Valid IdStrReq req) {
        return service.deleteReactive(new QSysLoginLog().owner.inOrEmpty(iLoginLogOwner.getOwners()).createTime.le(
            LocalDateTime.of(LocalDate.now().minusDays(SysLogController.MIN_DAY), LocalTime.of(0, 0, 0))).id.in(
            req.getId())).map(R::ok);
    }

}
