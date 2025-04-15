package org.start2do.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.dto.R;
import org.start2do.ebean.util.SysSettingUtil;

@RestController
@RequiredArgsConstructor
@RequestMapping("/system/config")
public class ConfigController {

    /**
     *
     */
    @GetMapping("sys/setting/reload")
    public R reloadSysSetting() {
        SysSettingUtil.getSysSettingUtil().sync();
        return R.ok();
    }
}
