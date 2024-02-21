package org.start2do.util;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class DictUtil {

    public static String getLabel(String type, String key) {
        if (JwtTokenUtil.IsWebFlux) {
            return DictReactiveUtil.getLabel(type, key);
        } else {
            return DictServletUtil.getLabel(type, key);
        }
    }

    public static ConcurrentHashMap<String, String> getItems(String type) {
        if (JwtTokenUtil.IsWebFlux) {
            return DictReactiveUtil.getItems(type);
        } else {
            return DictServletUtil.getItems(type);
        }
    }


}
