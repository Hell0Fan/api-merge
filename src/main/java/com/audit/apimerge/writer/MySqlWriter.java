package com.audit.apimerge.writer;

import com.audit.apimerge.model.MergedApi;
import java.sql.*;
import java.util.List;

/**
 * MySQL 数据写入器
 * 将合并后的 API 结果写入 MySQL 数据库
 */
public class MySqlWriter {
    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final String tableName;

    // 允许的表名格式：字母或下划线开头，后续可包含字母、数字、下划线
    private static final String VALID_TABLE_PATTERN = "^[a-zA-Z_][a-zA-Z0-9_]*$";

    /**
     * 构造函数
     * @param jdbcUrl   JDBC连接URL
     * @param username  用户名
     * @param password  密码
     * @param tableName 目标表名
     */
    public MySqlWriter(String jdbcUrl, String username, String password, String tableName) {
        validateTableName(tableName);
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.tableName = tableName;
    }

    /**
     * 校验表名，防止SQL注入
     * @param tableName 表名
     * @throws IllegalArgumentException 表名格式非法
     */
    private void validateTableName(String tableName) {
        if (tableName == null || !tableName.matches(VALID_TABLE_PATTERN)) {
            throw new IllegalArgumentException("Invalid table name: " + tableName);
        }
    }

    /**
     * 批量写入合并结果
     * @param apis 合并后的API列表
     */
    public void write(List<MergedApi> apis) {
        if (apis.isEmpty()) return;

        // 插入语句：merged_url, app, method, original_urls, source_count, merge_type, create_time
        String sql = "INSERT INTO " + tableName +
            " (merged_url, app, method, original_urls, source_count, merge_type, create_time) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // 批量设置参数并执行
            for (MergedApi api : apis) {
                ps.setString(1, api.getMergedUrl());
                ps.setString(2, api.getApp() != null ? api.getApp() : "default");
                ps.setString(3, api.getMethod());
                ps.setString(4, api.getOriginalUrls());
                ps.setInt(5, api.getSourceCount());
                ps.setString(6, api.getMergeType());
                ps.setLong(7, api.getCreateTime());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to write to MySQL", e);
        }
    }
}