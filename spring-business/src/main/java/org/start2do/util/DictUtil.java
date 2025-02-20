package org.start2do.util;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "start2do.business.service", name = "dict", havingValue = "true", matchIfMissing = true)
public class DictUtil {

    public static String getLabel(String type, String key) {
        return DictServletUtil.getLabel(type, key);
    }

    public static ConcurrentHashMap<String, String> getItems(String type) {
        return DictServletUtil.getItems(type);
    }


}
