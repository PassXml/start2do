package org.start2do.ebean.enums;

import java.util.Optional;
import org.start2do.dto.BusinessException;
import org.start2do.ebean.dict.DictItems;
import org.start2do.ebean.dict.IDictItem;

public enum YesOrNoType implements IDictItem {
    Yes("1", "是"), No("0", "否"),

    ;


    YesOrNoType(String value, String label) {
        putItemBean(value, label);
    }

    public static YesOrNoType get(String value) {
        return find(value).orElseThrow(() -> new BusinessException("未知字典值:" + value));
    }

    public static Optional<YesOrNoType> find(String value) {
        YesOrNoType result = DictItems.getByValue(YesOrNoType.class, value);
        if (result == null) {
            return Optional.empty();
        }
        return Optional.of(result);
    }
}
