package org.start2do.ebean.dict;

import io.ebean.annotation.DbEnumType;
import io.ebean.annotation.DbEnumValue;
import org.start2do.util.StringUtils;

public interface IDictItem {

    default void putItemBean(String value, String label) {
        StaticDictPool.putDictItem(this, value, label);
    }

    default DictItemBean getItemBean() {
        return StaticDictPool.getDictItem(this);
    }

    default String value() {
        return getItemBean().getValue();
    }

    default String label() {
        return getItemBean().getLabel();
    }

    @DbEnumValue(length = 2, storage = DbEnumType.VARCHAR)
    default String getValue() {
        return getItemBean().getValue();
    }

    default boolean isValue(String value) {
        return StringUtils.isNotBlank(value) && value.endsWith(value());
    }

}
