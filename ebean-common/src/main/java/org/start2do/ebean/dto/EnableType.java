package org.start2do.ebean.dto;

import org.start2do.dto.BusinessException;
import org.start2do.ebean.dict.IDictItem;

public enum EnableType implements IDictItem {
    Enable("1", "启用"),
    DisEnable("0", "停用"),

    ;

    EnableType(String value, String label) {
        putItemBean(value, label);
    }

    public static EnableType find(String s) {
        for (EnableType value : values()) {
            if (value != null && value.getValue().equals(s)) {
                return value;
            }
        }
        throw new BusinessException(String.format("%s未知字典值:%s", "EnableType", s));
    }
}
