package org.start2do.service;

import java.util.Map;

public interface ISysLoginUserCustomInfoService {

    Map<String, Object> getCustomInfo(Integer userId);
}
