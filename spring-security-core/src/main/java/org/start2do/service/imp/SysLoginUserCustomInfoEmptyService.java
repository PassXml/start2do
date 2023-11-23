package org.start2do.service.imp;

import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.start2do.service.ISysLoginUserCustomInfoService;

@Service
@ConditionalOnProperty(name = "jwt.enable", havingValue = "true")
@ConditionalOnMissingBean(SysLoginUserCustomInfoEmptyService.class)
public class SysLoginUserCustomInfoEmptyService implements ISysLoginUserCustomInfoService {

    public Map<String, Object> getCustomInfo(Integer userId) {
        return null;
    }
}
