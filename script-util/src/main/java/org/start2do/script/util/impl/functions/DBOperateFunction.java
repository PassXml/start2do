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
                    log.error("关闭连接失败", e);
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

    /**
     * 注入数据源,查询sql,并且通过Map返回结果
     *
     * @param dataSource
     * @param sql
     * @param params
     * @return
     */
    public static List<Object> query(DataSource dataSource, String sql, List<Object> params) {
        try {
            return query(dataSource.getConnection(), sql, params);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
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
        if (StringUtils.isEmpty(sql)) {
            return result;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            if (params != null) {
                for (int i = 0; i < params.size(); i++) {
                    preparedStatement.setObject(i + 1, params.get(i));
                }
            }
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                while (resultSet.next()) {
                    if (columnCount > 1) {
                        Map<String, Object> map = new HashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            map.put(metaData.getColumnName(i), resultSet.getObject(i));
                        }
                        result.add(map);
                    } else {
                        result.add(resultSet.getObject(1));
                    }
                }
            }
        } catch (SQLException e) {
            log.error("查询失败,{}", e.getMessage());
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
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    preparedStatement.setObject(i + 1, params[i]);
                }
            }
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
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
        try {
            return execute(dataSource.getConnection(), sql, params);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
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
        try {
            return execute(dataSource.getConnection(), sql, params);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        return 0;
    }

    /**
     * 获取jdbc连接
     */
    public static Connection getConn(String clazz, String jdbcUrl, String username, String password) {
        return connectCache.get(String.join(",", clazz, jdbcUrl, username, password), s -> {
            try {
                Class<?> aClass = Class.forName(clazz);
                return DriverManager.getConnection(jdbcUrl, username, password);
            } catch (Exception e) {
                log.error("获取连接发生错误,{}", e.getMessage());
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
        return dataSourceCaffeine.get(String.join(",", jdbcUrl, username, password), s -> {
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
            log.error("关闭连接失败,{}", e.getMessage());
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
            log.error("获取连接失败,{}", e.getMessage());
        }
        return null;
    }
}
