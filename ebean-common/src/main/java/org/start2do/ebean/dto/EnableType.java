package org.start2do.ebean.dto;

import io.ebean.annotation.DbEnumValue;
import org.start2do.dto.BusinessException;

public enum EnableType {
    Enable("1", "启用"),
    DisEnable("0", "停用"),

    ;
    private String value;
    private String label;

    EnableType(String value, String label) {
        this.value = value;
        this.label = label;
    }

    @DbEnumValue(length = 2)
    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static EnableType find(String s) {
        for (EnableType value : values()) {
            if (value.getValue().equals(s)) {
                return value;
            }
        }
        throw new BusinessException(String.format("%s未知字典值:%s", "EnableType", s));
    }
}
