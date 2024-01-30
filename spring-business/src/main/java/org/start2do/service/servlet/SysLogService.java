package org.start2do.service.servlet;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.stereotype.Service;
import org.start2do.ebean.service.AbsService;
import org.start2do.entity.business.SysLog;

@Service
@ConditionalOnWebApplication(type = Type.SERVLET)
public class SysLogService extends AbsService<SysLog> {

}
