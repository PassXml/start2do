package org.start2do.service.webflux;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.stereotype.Service;
import org.start2do.ebean.service.AbsMixService;
import org.start2do.entity.business.SysLog;

@Service
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnProperty(prefix = "start2do.business.service", name = "log", havingValue = "true",matchIfMissing = true)
public class SysLogReactiveService extends AbsMixService<SysLog, String> {

}
