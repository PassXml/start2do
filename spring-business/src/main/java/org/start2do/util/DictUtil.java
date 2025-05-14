package org.start2do.util;

import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    prefix = "start2do.business.service",
    name = "dict",
    havingValue = "true",
    matchIfMissing = true)
public class DictUtil {

  public static String getLabel(String type, String key) {
    if (StringUtils.isEmpty(key)) {
      return null;
    }
    return DictServletUtil.getLabel(type, key);
  }

  public static ConcurrentHashMap<String, String> hasValue(String type) {
    return DictServletUtil.getItems(type);
  }

  public static boolean hasKey(String type, String key) {
    ConcurrentHashMap<String, String> map = DictServletUtil.getItems(type);
    if (map == null) {
      return false;
    }
    return map.contains(key);
  }

  public static boolean hasValue(String type, String value) {
    ConcurrentHashMap<String, String> map = DictServletUtil.getItems(type);
    if (map == null) {
      return false;
    }
    while (map.keys().hasMoreElements()) {
      String s = map.keys().nextElement();
      if (value.equals(s)) {
        return true;
      }
    }
    return false;
  }
}
