package org.start2do.script.util.impl.functions;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.start2do.util.StringUtils;

@Slf4j
public class DBOperateFunction {

    private static Cache<String, HikariDataSource> dataSourceCaffeine;
    private static final Cache<String, Connection> connectCache = Caffeine.newBuilder()
        .expireAfterAccess(Duration.of(5, ChronoUnit.MINUTES)).removalListener((key, value, cause) -> {
            if (value instanceof Connection) {
                boolean closed = false;
                try {
                    closed = ((Connection) value).isClosed();
                    if (!closed) {
                        ((Connection) value).close();
                    }
                } catch (SQLException e) {
                    log.error("DB操作失败, action=closeCachedConnection, cacheKey={}", key, e);
                }

            }
        }).build();

    public static void EnableHikariDataSource() {
        dataSourceCaffeine = Caffeine.newBuilder().expireAfterAccess(Duration.of(5, ChronoUnit.MINUTES))
            .removalListener((key, value, cause) -> {
                if (value instanceof HikariDataSource) {
                    ((HikariDataSource) value).close();
                }
            }).build();
    }

    private static Cache<String, HikariDataSource> getOrInitDataSourceCache() {
        if (dataSourceCaffeine == null) {
            synchronized (DBOperateFunction.class) {
                if (dataSourceCaffeine == null) {
                    EnableHikariDataSource();
                }
            }
        }
        return dataSourceCaffeine;
    }

    /**
     * 注入数据源,查询sql,并且通过Map返回结果
     *
     * @param dataSource
     * @param sql
     * @param params
     * @return
     */
    public static List<Object> query(DataSource dataSource, String sql, List<Object> params) {
        if (dataSource == null) {
            log.warn("dataSource为空,查询失败");
            return new ArrayList<>();
        }
        try (Connection connection = dataSource.getConnection()) {
            return query(connection, sql, params);
        } catch (SQLException e) {
            log.error("DB操作失败, action=queryByDataSource, sql={}, params={}", sql, params, e);
        }
        return new ArrayList<>();
    }


    /**
     * 注入数据源,查询sql,并且通过Map返回结果
     *
     * @param connection
     * @param sql
     * @param params
     * @return
     */
    public static List<Object> query(Connection connection, String sql, List<Object> params) {
        List<Object> result = new ArrayList<>();
        if (connection == null || StringUtils.isEmpty(sql)) {
            return result;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            bindParams(preparedStatement, params);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                while (resultSet.next()) {
                    if (columnCount > 1) {
                        result.add(toRowMap(resultSet, metaData, columnCount));
                    } else {
                        result.add(resultSet.getObject(1));
                    }
                }
            }
        } catch (SQLException e) {
            log.error("DB操作失败, action=queryByConnection, sql={}, params={}", sql, params, e);
        }
        return result;
    }

    /**
     * 执行sql
     *
     * @param connection 连接
     * @param sql        sql
     * @param params     参数
     * @return
     */
    public static int execute(Connection connection, String sql, Object... params) {
        if (connection == null || StringUtils.isEmpty(sql)) {
            return 0;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            bindParams(preparedStatement, params);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error("DB操作失败, action=executeByConnection, sql={}, params={}", sql, params, e);
        }
        return 0;
    }


    /**
     * 使用数据源执行SQL语句
     *
     * @param dataSource 数据源
     * @param sql        要执行的SQL语句
     * @param params     SQL语句中的参数
     * @return 受影响的行数
     */
    public static int execute(DataSource dataSource, String sql, Object... params) {
        if (dataSource == null) {
            log.warn("dataSource为空,执行失败");
            return 0;
        }
        try (Connection connection = dataSource.getConnection()) {
            return execute(connection, sql, params);
        } catch (SQLException e) {
            log.error("DB操作失败, action=executeByDataSource, sql={}, params={}", sql, params, e);
        }
        return 0;
    }

    /**
     * 更新数据
     *
     * @param dataSource 数据呀
     * @param sql        sql
     * @param params     参数
     * @return 受影响的行数
     */
    public static int update(DataSource dataSource, String sql, Object... params) {
        return execute(dataSource, sql, params);
    }

    /**
     * 获取jdbc连接
     */
    public static Connection getConn(String clazz, String jdbcUrl, String username, String password) {
        return connectCache.get(String.join(",", clazz, jdbcUrl, username, password), s -> {
            try {
                Class.forName(clazz);
                return DriverManager.getConnection(jdbcUrl, username, password);
            } catch (Exception e) {
                log.error("DB操作失败, action=getJdbcConnection, driver={}, jdbcUrl={}, username={}",
                    clazz, jdbcUrl, username, e);
            }
            return null;
        });
    }

    /**
     * 传入jdbcUrl连接和用户名密码
     *
     * @param jdbcUrl
     * @param username
     * @param password
     * @return
     */
    public static DataSource createDataSource(String jdbcUrl, String username, String password) {
        return getOrInitDataSourceCache().get(String.join(",", jdbcUrl, username, password), s -> {
            HikariConfig config = new HikariConfig();
            //最大连接数2个
            config.setMaximumPoolSize(2);
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);
            return new HikariDataSource(config);
        });
    }

    /**
     * 关闭连接
     *
     * @param connection
     */
    public static void close(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            log.error("DB操作失败, action=closeConnection", e);
        }
    }

    /**
     * 获取连接
     *
     * @param dataSource
     * @return
     */
    public static Connection getConn(DataSource dataSource) {
        if (dataSource == null) {
            log.warn("dataSource为空,获取连接失败");
            return null;
        }
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            log.error("DB操作失败, action=getConnectionByDataSource", e);
        }
        return null;
    }

    public static List<Map<String, Object>> queryRows(DataSource dataSource, String sql, Object... params) {
        if (dataSource == null) {
            log.warn("dataSource为空,查询失败");
            return new ArrayList<>();
        }
        try (Connection connection = dataSource.getConnection()) {
            return queryRows(connection, sql, params);
        } catch (SQLException e) {
            log.error("DB操作失败, action=queryRowsByDataSource, sql={}, params={}", sql, params, e);
        }
        return new ArrayList<>();
    }

    public static List<Map<String, Object>> queryRows(Connection connection, String sql, Object... params) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (connection == null || StringUtils.isEmpty(sql)) {
            return result;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            bindParams(preparedStatement, params);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                while (resultSet.next()) {
                    result.add(toRowMap(resultSet, metaData, columnCount));
                }
            }
        } catch (SQLException e) {
            log.error("DB操作失败, action=queryRowsByConnection, sql={}, params={}", sql, params, e);
        }
        return result;
    }

    public static Map<String, Object> queryOne(DataSource dataSource, String sql, Object... params) {
        List<Map<String, Object>> rows = queryRows(dataSource, sql, params);
        if (rows.isEmpty()) {
            return new HashMap<>();
        }
        return rows.get(0);
    }

    public static Object queryValue(DataSource dataSource, String sql, Object... params) {
        if (dataSource == null) {
            log.warn("dataSource为空,查询失败");
            return null;
        }
        try (Connection connection = dataSource.getConnection()) {
            return queryValue(connection, sql, params);
        } catch (SQLException e) {
            log.error("DB操作失败, action=queryValueByDataSource, sql={}, params={}", sql, params, e);
        }
        return null;
    }

    public static Object queryValue(Connection connection, String sql, Object... params) {
        if (connection == null || StringUtils.isEmpty(sql)) {
            return null;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            bindParams(preparedStatement, params);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getObject(1);
                }
            }
        } catch (SQLException e) {
            log.error("DB操作失败, action=queryValueByConnection, sql={}, params={}", sql, params, e);
        }
        return null;
    }

    public static boolean exists(DataSource dataSource, String sql, Object... params) {
        return queryValue(dataSource, sql, params) != null;
    }

    private static void bindParams(PreparedStatement preparedStatement, List<Object> params) throws SQLException {
        if (params == null) {
            return;
        }
        for (int i = 0; i < params.size(); i++) {
            preparedStatement.setObject(i + 1, params.get(i));
        }
    }

    private static void bindParams(PreparedStatement preparedStatement, Object... params) throws SQLException {
        if (params == null) {
            return;
        }
        for (int i = 0; i < params.length; i++) {
            preparedStatement.setObject(i + 1, params[i]);
        }
    }

    private static Map<String, Object> toRowMap(ResultSet resultSet, ResultSetMetaData metaData, int columnCount)
        throws SQLException {
        Map<String, Object> map = new HashMap<>();
        for (int i = 1; i <= columnCount; i++) {
            map.put(metaData.getColumnLabel(i), resultSet.getObject(i));
        }
        return map;
    }
}
