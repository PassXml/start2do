package org.start2do.controller.servlet;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
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
import org.start2do.service.servlet.SysLoginLogService;

/**
 * 登陆日志
 */
@Slf4j
@RestController
@RequestMapping("sys/login/log")
@RequiredArgsConstructor
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "start2do.business.controller", name = "login-log",havingValue = "true",matchIfMissing = true)
public class SysLoginLogController {

    private final SysLoginLogService service;

    /**
     * 分页
     */
    @GetMapping("page")
    public R<Page<SysLogPageResp>> page(SysLoginLogReq req) {
        QSysLoginLog qClass = new QSysLoginLog().createTime.geIfPresent(req.getStartTime()).createTime.ltIfPresent(
            req.getEndTime());
        Where.ready().like(req.getUsername(), qClass.username).like(req.getIp(), qClass.ip);
        return R.ok(service.page(qClass, req, SysLoginLogDtoMapper.INSTANCE::toSysLogPageResp));
    }

    /**
     * 详情
     */
    @GetMapping("detail")
    public R<SysLogPageResp> detail(String id) {
        return R.ok(SysLoginLogDtoMapper.INSTANCE.toSysLogPageResp(service.getById(id)));
    }

    /**
     * 散场
     */
    @GetMapping("delete")
    public R delete(@Valid IdStrReq req) {
        service.delete(new QSysLoginLog().createTime.le(
            LocalDateTime.of(LocalDate.now().minusDays(SysLogController.MIN_DAY), LocalTime.of(0, 0, 0))).id.in(
            req.getId()));
        return R.ok();
    }

}
