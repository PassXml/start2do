package org.start2do.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class OSUtil {
    public static boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }
}
