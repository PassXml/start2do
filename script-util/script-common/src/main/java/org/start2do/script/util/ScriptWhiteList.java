package org.start2do.script.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.start2do.script.util.impl.functions.DBOperateFunction;
import org.start2do.script.util.impl.functions.HttpUtil;
import org.start2do.script.util.impl.functions.JacksonOperateFunction;
import org.start2do.script.util.impl.functions.SqlOperateFunction;

public final class ScriptWhiteList {

    private static final Set<String> DEFAULT_WHITE_LIST = new LinkedHashSet<>(Arrays.asList(
        Object.class.getName(),
        String.class.getName(),
        CharSequence.class.getName(),
        Boolean.class.getName(),
        Byte.class.getName(),
        Character.class.getName(),
        Number.class.getName(),
        Integer.class.getName(),
        Long.class.getName(),
        Double.class.getName(),
        Float.class.getName(),
        Short.class.getName(),
        StringBuilder.class.getName(),
        StringBuffer.class.getName(),
        Comparable.class.getName(),
        Iterable.class.getName(),
        BigDecimal.class.getName(),
        BigInteger.class.getName(),
        Math.class.getName(),
        UUID.class.getName(),
        Collection.class.getName(),
        List.class.getName(),
        Map.class.getName(),
        Set.class.getName(),
        Iterator.class.getName(),
        Collections.class.getName(),
        Objects.class.getName(),
        Optional.class.getName(),
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
