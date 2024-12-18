package org.start2do.service.servlet;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.stereotype.Service;
import org.start2do.ebean.service.AbsService;
import org.start2do.entity.business.SysLog;

@Service
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnProperty(prefix = "start2do.business.service", name = "log", havingValue = "true")
public class SysLogService extends AbsService<SysLog> {

}
