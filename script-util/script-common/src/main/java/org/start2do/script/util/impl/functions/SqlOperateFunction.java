package org.start2do.script.util.impl.functions;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * SQL工具函数，供脚本引擎生成分页任务SQL。
 */
public final class SqlOperateFunction {

    private static final int DEFAULT_PAGE_NO = 1;
    private static final int DEFAULT_PAGE_SIZE = 100;

    private SqlOperateFunction() {
    }

    public static int offset(int pageNo, int pageSize) {
        int safePageNo = normalizePageNo(pageNo);
        int safePageSize = normalizePageSize(pageSize);
        long offset = (long) (safePageNo - 1) * safePageSize;
        return offset > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) offset;
    }

    public static int totalPages(long total, int pageSize) {
        if (total <= 0) {
            return 0;
        }
        int safePageSize = normalizePageSize(pageSize);
        long pages = (total + safePageSize - 1) / safePageSize;
        return pages > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) pages;
    }

    public static boolean hasNext(int pageNo, int pageSize, long total) {
        return normalizePageNo(pageNo) < totalPages(total, pageSize);
    }

    public static Map<String, Object> pageTask(int pageNo, int pageSize, long total) {
        int safePageNo = normalizePageNo(pageNo);
        int safePageSize = normalizePageSize(pageSize);
        int pages = totalPages(total, safePageSize);
        Map<String, Object> task = new LinkedHashMap<>();
        task.put("pageNo", safePageNo);
        task.put("pageSize", safePageSize);
        task.put("offset", offset(safePageNo, safePageSize));
        task.put("limit", safePageSize);
        task.put("total", Math.max(total, 0));
        task.put("totalPages", pages);
        task.put("hasNext", safePageNo < pages);
        return task;
    }

    public static String countSql(String sql) {
        String targetSql = trimSql(sql);
        if (targetSql.isEmpty()) {
            return "";
        }
        return "SELECT COUNT(1) FROM (" + targetSql + ") page_count";
    }

    public static String pageSql(String sql, int pageNo, int pageSize) {
        return pageSql(sql, pageNo, pageSize, "mysql");
    }

    public static String pageSql(String sql, int pageNo, int pageSize, String dialect) {
        String targetSql = trimSql(sql);
        if (targetSql.isEmpty()) {
            return "";
        }
        int safePageSize = normalizePageSize(pageSize);
        int safeOffset = offset(pageNo, safePageSize);
        String normalizedDialect = normalizeDialect(dialect);
        switch (normalizedDialect) {
            case "oracle":
            case "oracle12":
            case "sqlserver":
            case "mssql":
                return targetSql + " OFFSET " + safeOffset + " ROWS FETCH NEXT " + safePageSize + " ROWS ONLY";
            case "postgres":
            case "postgresql":
            case "sqlite":
            case "h2":
                return targetSql + " LIMIT " + safePageSize + " OFFSET " + safeOffset;
            case "mysql":
            case "mariadb":
            case "clickhouse":
            default:
                return targetSql + " LIMIT " + safeOffset + ", " + safePageSize;
        }
    }

    private static int normalizePageNo(int pageNo) {
        return Math.max(pageNo, DEFAULT_PAGE_NO);
    }

    private static int normalizePageSize(int pageSize) {
        return pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
    }

    private static String normalizeDialect(String dialect) {
        if (dialect == null || dialect.trim().isEmpty()) {
            return "mysql";
        }
        return dialect.trim().toLowerCase(Locale.ROOT);
    }

    private static String trimSql(String sql) {
        if (sql == null) {
            return "";
        }
        String targetSql = sql.trim();
        while (targetSql.endsWith(";")) {
            targetSql = targetSql.substring(0, targetSql.length() - 1).trim();
        }
        return targetSql;
    }
}
