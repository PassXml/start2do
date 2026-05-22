package org.start2do.script.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.start2do.script.util.impl.functions.DBOperateFunction;
import org.start2do.script.util.impl.functions.HttpUtil;
import org.start2do.script.util.impl.functions.JacksonOperateFunction;
import org.start2do.script.util.impl.functions.SqlOperateFunction;

public final class ScriptWhiteList {

    private static final Set<String> DEFAULT_WHITE_LIST = new LinkedHashSet<>(Arrays.asList(
        String.class.getName(),
        Boolean.class.getName(),
        Integer.class.getName(),
        Long.class.getName(),
        Double.class.getName(),
        Float.class.getName(),
        Short.class.getName(),
        BigDecimal.class.getName(),
        BigInteger.class.getName(),
        Math.class.getName(),
        UUID.class.getName(),
        ArrayList.class.getName(),
        HashMap.class.getName(),
        HashSet.class.getName(),
        LinkedHashMap.class.getName(),
        LinkedHashSet.class.getName(),
        LocalDate.class.getName(),
        LocalTime.class.getName(),
        LocalDateTime.class.getName(),
        DateTimeFormatter.class.getName(),
        HttpUtil.class.getName(),
        JacksonOperateFunction.class.getName(),
        DBOperateFunction.class.getName(),
        SqlOperateFunction.class.getName()
    ));

    private ScriptWhiteList() {
    }

    public static Set<String> defaultWhiteList() {
        return new LinkedHashSet<>(DEFAULT_WHITE_LIST);
    }
}
