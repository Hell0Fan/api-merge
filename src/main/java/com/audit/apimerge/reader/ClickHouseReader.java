package com.audit.apimerge.reader;

import com.audit.apimerge.model.ApiRecord;
import ru.yandex.clickhouse.ClickHouseDriver;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ClickHouse 数据读取器
 * 用于从 audit_record 表增量读取 API 记录
 */
public class ClickHouseReader {
    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final int batchSize;  // 每批查询的记录数

    // 静态初始化：注册 ClickHouse JDBC 驱动
    static {
        try {
            DriverManager.registerDriver(new ClickHouseDriver());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to register ClickHouse driver", e);
        }
    }

    /**
     * 构造函数
     * @param jdbcUrl   JDBC连接URL
     * @param username  用户名
     * @param password  密码
     * @param batchSize 每批查询的最大记录数
     */
    public ClickHouseReader(String jdbcUrl, String username, String password, int batchSize) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.batchSize = batchSize;
    }

    /**
     * 查询指定ID之后的记录
     * @param lastProcessedId 上次处理的最大ID
     * @return API记录列表
     */
    public List<ApiRecord> queryAfterId(long lastProcessedId) {
        List<ApiRecord> records = new ArrayList<>();
        // SQL：查询ID大于lastProcessedId的记录，按ID排序并限制数量
        String sql = "SELECT id, url, method, name, create_time FROM audit_record " +
                     "WHERE id > ? ORDER BY ID LIMIT ?";

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, lastProcessedId);
            ps.setInt(2, batchSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    String url = rs.getString("url");
                    String method = rs.getString("method");
                    String name = rs.getString("name");
                    long createTime = rs.getTimestamp("create_time") != null
                        ? rs.getTimestamp("create_time").getTime()
                        : 0;
                    // 从URL中提取应用标识（第一级路径，如 /api/shop/... -> shop）
                    String app = extractAppFromUrl(url);
                    records.add(new ApiRecord(id, url, method, name, createTime, app));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query ClickHouse", e);
        }
        return records;
    }

    /**
     * 从URL中提取应用标识
     * 例如：/api/shop/user/123 -> shop
     *      /api/v2/products/list -> v2
     * @param url 原始URL
     * @return 应用标识
     */
    private String extractAppFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "unknown";
        }
        String[] parts = url.split("/");
        // 通常第二级是应用标识（跳过第一级空字符串）
        // 如 /api/shop/... -> parts[1]="api", parts[2]="shop"
        if (parts.length >= 3) {
            return parts[2];
        }
        // 如果URL格式简单，使用第一级
        if (parts.length >= 2) {
            return parts[1];
        }
        return "unknown";
    }
}