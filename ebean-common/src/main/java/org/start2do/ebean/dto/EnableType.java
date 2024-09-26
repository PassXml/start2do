package org.start2do.ebean.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.start2do.constant.Constant;
import org.start2do.dto.BusinessException;
import org.start2do.ebean.dict.IDictItem;

public enum EnableType implements IDictItem {
    Enable(Constant.ENABLE, "启用"), DisEnable(Constant.DISABLE, "停用"),

    ;

    EnableType(String value, String label) {
        putItemBean(value, label);
    }

    @JsonCreator
    public static EnableType find(String s) {
        for (EnableType value : values()) {
            if (value != null && value.getValue().equals(s)) {
                return value;
            }
        }
        throw new BusinessException(String.format("%s未知字典值:%s", "EnableType", s));
    }

    public static EnableType get(String s) {
        for (EnableType value : values()) {
            if (value != null && value.getValue().equals(s)) {
                return value;
            }
        }
        throw new BusinessException(String.format("%s未知字典值:%s", "EnableType", s));
    }


    @Override
    public String getDesc() {
        return "是否启用";
    }
}
