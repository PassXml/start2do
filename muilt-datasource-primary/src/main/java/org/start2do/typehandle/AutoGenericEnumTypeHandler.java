package org.start2do.typehandle;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.start2do.ebean.dict.DictItems;
import org.start2do.ebean.dict.IDictItem;

public class AutoGenericEnumTypeHandler<E extends IDictItem> extends BaseTypeHandler<E> {


    private Class<E> enumType;

    protected Class getTClass() {
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        return (Class<E>) actualTypeArguments[0];
    }

    public AutoGenericEnumTypeHandler() {
        enumType = getTClass();
    }

    public AutoGenericEnumTypeHandler(Class<E> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.enumType = type;
    }

    private E loadEnumByValue(String value) {
        return DictItems.getByValue(enumType, value);
    }

    /**
     * 用于定义设置参数时，该如何把Java类型的参数转换为对应的数据库类型
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.value());
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        if (value == null) {
            return null;
        }
        return loadEnumByValue(value);
    }


    /**
     * 用于定义设置参数时，该如何把Java类型的参数转换为对应的数据库类型
     */
    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        if (rs.getObject(columnIndex) == null) {
            return null;
        }
        String value = rs.getString(columnIndex);
        return loadEnumByValue(value);
    }

    /**
     * 用于定义设置参数时，该如何把Java类型的参数转换为对应的数据库类型
     */
    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        if (cs.getObject(columnIndex) == null) {
            return null;
        }
        String value = cs.getString(columnIndex);
        return loadEnumByValue(value);
    }
}
