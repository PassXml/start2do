package org.start2do.ebean.service;

import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.start2do.ebean.entity.SysSetting;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnBean(DataSource.class)
public class SysSettingService extends AbsService<SysSetting> {

}
