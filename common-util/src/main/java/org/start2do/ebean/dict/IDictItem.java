package org.start2do.ebean.dict;

import com.fasterxml.jackson.annotation.JsonValue;
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

    @JsonValue
    @DbEnumValue(length = 2, storage = DbEnumType.VARCHAR, withConstraint = false)
    default String getValue() {
        return getItemBean().getValue();
    }

    default String getLabel() {
        return getItemBean().getLabel();
    }

    default boolean isValue(String value) {
        return StringUtils.isNotBlank(value) && value.endsWith(value());
    }

    static <T extends IDictItem> T find(Class<T> tClass, String value) {
        return DictItems.getByValue(tClass, value);
    }

    default String convert(IDictItem s) {
        return s.value();
    }

    default String getClassName() {
        return null;
    }

    default String getDesc() {
        return null;
    }

}
