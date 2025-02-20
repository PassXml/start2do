package org.start2do;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StaticExecutor {

    public static String getInfo(String a, Integer b) {
        return "你好" + a + b;
    }
}
