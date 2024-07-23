package org.start2do.controller.webflux;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.dto.R;
import org.start2do.dto.mapper.SettingDtoMapper;
import org.start2do.dto.resp.setting.SettingMenuResp;
import org.start2do.ebean.dto.EnableType;
import org.start2do.ebean.entity.query.QSysSetting;
import org.start2do.ebean.service.SysSettingService;
import reactor.core.publisher.Mono;

/**
 * 系统设置
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("sys/setting")
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnProperty(prefix = "start2do.business.controller", name = "setting", havingValue = "true")
public class SysSettingCustomController {

    private final SysSettingService settingService;

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
