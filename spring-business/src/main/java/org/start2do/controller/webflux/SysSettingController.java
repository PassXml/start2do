package org.start2do.controller.webflux;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.dto.IdReq;
import org.start2do.dto.Page;
import org.start2do.dto.R;
import org.start2do.dto.mapper.SettingDtoMapper;
import org.start2do.dto.req.setting.SettingAddReq;
import org.start2do.dto.req.setting.SettingPageReq;
import org.start2do.dto.req.setting.SettingUpdateReq;
import org.start2do.dto.resp.setting.SettingDetailResp;
import org.start2do.dto.resp.setting.SettingMenuResp;
import org.start2do.dto.resp.setting.SettingPageResp;
import org.start2do.ebean.dto.EnableType;
import org.start2do.ebean.entity.SysSetting;
import org.start2do.ebean.entity.query.QSysSetting;
import org.start2do.ebean.service.SysSettingService;
import org.start2do.ebean.util.Where;
import org.start2do.util.BeanValidatorUtil;
import reactor.core.publisher.Mono;

/**
 * 系统设置
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("sys/setting")
@ConditionalOnWebApplication(type = Type.REACTIVE)

public class SysSettingController {

    private final SysSettingService settingService;

    /**
     * 分页
     */
    @GetMapping("page")
    public Mono<R<Page<SettingPageResp>>> page(SettingPageReq req) {
        QSysSetting qClass = new QSysSetting();
        Where.ready().like(req.getKey(), qClass.key::like).notEmpty(req.getType(), qClass.type::eq)
            .like(req.getValue(), qClass.value::like);
        return Mono.just(settingService.page(qClass, req, SettingDtoMapper.INSTANCE::toSettingResp)).map(R::ok);
    }

    /**
     * 添加
     */
    @PostMapping("add")
    public Mono<R<Boolean>> add(@RequestBody SettingAddReq req) {
        BeanValidatorUtil.validate(req);
        return Mono.fromCallable(() -> {
            settingService.save(SettingDtoMapper.INSTANCE.toEntity(req));
            return true;
        }).map(R::ok);
    }

    /**
     * 更新
     */
    @PostMapping("update")
    public Mono<R<Boolean>> update(@RequestBody SettingUpdateReq req) {
        BeanValidatorUtil.validate(req);
        return Mono.fromCallable(() -> {
            SysSetting setting = settingService.getById(req.getId());
            SettingDtoMapper.INSTANCE.update(setting, req);
            settingService.update(setting);
            return true;
        }).map(R::ok);
    }

    /**
     * 删除
     */
    @GetMapping("delete")
    public Mono<R<Integer>> delete(IdReq req) {
        BeanValidatorUtil.validate(req);
        return Mono.fromCallable(() -> settingService.deleteById(req.getId())).map(R::ok);
    }

    /**
     * 详情
     */
    @GetMapping("detail")
    public Mono<R<SettingDetailResp>> detail(IdReq req) {
        BeanValidatorUtil.validate(req);
        return Mono.fromCallable(
            () -> SettingDtoMapper.INSTANCE.toDetail(settingService.getById(req.getId()))).map(R::ok);
    }

    /**
     * 所有系统设置
     */
    @GetMapping("all")
    public Mono<R<List<SettingMenuResp>>> all() {
        return Mono.fromCallable(
            () -> settingService.findAll(new QSysSetting().enable.eq(EnableType.Enable)).stream()
                .map(SettingDtoMapper.INSTANCE::toSettingMenuResp).toList()).map(R::ok);
    }

}