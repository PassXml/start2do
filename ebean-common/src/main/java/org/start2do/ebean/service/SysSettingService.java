package org.start2do.ebean.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.start2do.ebean.entity.SysSetting;

@Service
@ConditionalOnProperty(prefix = "start2do.ebean", name = {"enable",
    "enable-setting-service"}, havingValue = "true", matchIfMissing = true)
public class SysSettingService extends AbsService<SysSetting> {

}
