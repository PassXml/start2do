package org.start2do.ebean.service;

import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.start2do.ebean.entity.SysSetting;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "start2do.ebean", name = {"enable",
    "enable-setting-service"}, havingValue = "true", matchIfMissing = true)
public class SysSettingService extends AbsService<SysSetting> {

}
