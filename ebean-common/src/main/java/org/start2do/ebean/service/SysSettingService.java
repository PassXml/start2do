package org.start2do.ebean.service;

import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.start2do.ebean.entity.SysSetting;

@Service
@ConditionalOnBean(DataSource.class)
public class SysSettingService extends AbsService<SysSetting> {

}
