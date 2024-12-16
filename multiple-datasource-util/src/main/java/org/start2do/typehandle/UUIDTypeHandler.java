package org.start2do.typehandle;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

public class UUIDTypeHandler extends BaseTypeHandler<UUID> {

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, UUID uuid, JdbcType jdbcType)
        throws SQLException {
        preparedStatement.setString(i, uuid.toString());
    }

    @Override
    public UUID getNullableResult(ResultSet resultSet, String i) throws SQLException {
        String value = resultSet.getString(i);
        if (value == null) {
            return null;
        }
        return UUID.fromString(value);
    }

    @Override
    public UUID getNullableResult(ResultSet resultSet, int i) throws SQLException {
        String value = resultSet.getString(i);
        if (value == null) {
            return null;
        }
        return UUID.fromString(value);
    }

    @Override
    public UUID getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        String value = callableStatement.getString(i);
        if (value == null) {
            return null;
        }
        return UUID.fromString(value);
    }
}
