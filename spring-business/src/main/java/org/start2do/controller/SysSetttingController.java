package org.start2do.controller;

import lombok.RequiredArgsConstructor;
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
import org.start2do.dto.resp.setting.SettingPageResp;
import org.start2do.ebean.entity.SysSetting;
import org.start2do.ebean.entity.query.QSysSetting;
import org.start2do.ebean.service.SysSettingService;
import org.start2do.ebean.util.Where;
import org.start2do.util.BeanValidatorUtil;

/**
 * 系统设置
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("sys/setting")
public class SysSetttingController {

    private final SysSettingService settingService;

    /**
     * 分页
     */
    @GetMapping("page")
    public R<Page<SettingPageResp>> page(SettingPageReq req) {
        QSysSetting qClass = new QSysSetting();
        Where.ready().like(req.getKey(), qClass.key::like)
            .notEmpty(req.getType(), qClass.type::eq).like(req.getValue(), qClass.value::like);
        return R.ok(settingService.page(qClass, req, SettingDtoMapper.INSTANCE::toSettingResp));
    }

    /**
     * 添加
     */
    @PostMapping("add")
    public R add(@RequestBody SettingAddReq req) {
        BeanValidatorUtil.validate(req);
        settingService.save(SettingDtoMapper.INSTANCE.toEntity(req));
        return R.ok();
    }

    /**
     * 更新
     */
    @PostMapping("update")
    public R update(@RequestBody SettingUpdateReq req) {
        BeanValidatorUtil.validate(req);
        SysSetting setting = settingService.getById(req.getId());
        SettingDtoMapper.INSTANCE.update(setting, req);
        settingService.update(setting);
        return R.ok();
    }

    /**
     * 删除
     */
    @GetMapping("delete")
    public R delete(IdReq req) {
        BeanValidatorUtil.validate(req);
        settingService.deleteById(req.getId());
        return R.ok();
    }

    /**
     * 详情
     */
    @GetMapping("detail")
    public R<SettingDetailResp> detail(IdReq req) {
        BeanValidatorUtil.validate(req);
        return R.ok(SettingDtoMapper.INSTANCE.toDetail(settingService.getById(req.getId())));
    }


}
