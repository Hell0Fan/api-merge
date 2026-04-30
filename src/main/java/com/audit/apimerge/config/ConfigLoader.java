package com.audit.apimerge.config;

import org.yaml.snakeyaml.Yaml;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

/**
 * 配置加载器
 * 从 YAML 文件加载应用配置
 */
public class ConfigLoader {

    /**
     * 从指定路径加载 YAML 配置
     * @param path 配置文件路径
     * @return AppConfig 实例
     * @throws IllegalArgumentException if path is null or empty
     * @throws RuntimeException if config file cannot be loaded or parsed
     */
    public static AppConfig load(String path) {
        Objects.requireNonNull(path, "Config file path cannot be null");
        if (path.isEmpty()) {
            throw new IllegalArgumentException("Config file path cannot be empty");
        }

        Yaml yaml = new Yaml();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(path);
            Map<String, Object> data = yaml.load(inputStream);
            if (data == null) {
                throw new RuntimeException("Config file is empty: " + path);
            }
            return mapToConfig(data);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Config file not found: " + path, e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    // Ignore close exception
                }
            }
        }
    }

    /**
     * 将 Map 数据映射到 AppConfig 对象
     * @param data 配置数据
     * @return AppConfig 实例
     */
    @SuppressWarnings("unchecked")
    private static AppConfig mapToConfig(Map<String, Object> data) {
        AppConfig config = new AppConfig();

        // 解析 ClickHouse 配置
        Map<String, Object> clickhouse = (Map<String, Object>) data.get("clickhouse");
        if (clickhouse != null) {
            String jdbcUrl = (String) clickhouse.get("jdbc-url");
            if (jdbcUrl == null || jdbcUrl.isEmpty()) {
                throw new IllegalArgumentException("ClickHouse jdbc-url is required");
            }
            config.setClickhouseJdbcUrl(jdbcUrl);
            config.setClickhouseUsername((String) clickhouse.getOrDefault("username", "default"));
            config.setClickhousePassword((String) clickhouse.getOrDefault("password", ""));
            
            Object batchSizeObj = clickhouse.get("batch-size");
            int batchSize = 1000; // default
            if (batchSizeObj != null) {
                if (batchSizeObj instanceof Number) {
                    batchSize = ((Number) batchSizeObj).intValue();
                    if (batchSize <= 0) {
                        throw new IllegalArgumentException("ClickHouse batch-size must be positive");
                    }
                } else {
                    try {
                        batchSize = Integer.parseInt(batchSizeObj.toString());
                        if (batchSize <= 0) {
                            throw new IllegalArgumentException("ClickHouse batch-size must be positive");
                        }
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("ClickHouse batch-size must be a valid integer", e);
                    }
                }
            }
            config.setBatchSize(batchSize);
        }

        // 解析 MySQL 配置
        Map<String, Object> mysql = (Map<String, Object>) data.get("mysql");
        if (mysql != null) {
            String jdbcUrl = (String) mysql.get("jdbc-url");
            if (jdbcUrl == null || jdbcUrl.isEmpty()) {
                throw new IllegalArgumentException("MySQL jdbc-url is required");
            }
            config.setMysqlJdbcUrl(jdbcUrl);
            
            String username = (String) mysql.get("username");
            if (username == null || username.isEmpty()) {
                throw new IllegalArgumentException("MySQL username is required");
            }
            config.setMysqlUsername(username);
            
            String password = (String) mysql.get("password");
            if (password == null) {
                throw new IllegalArgumentException("MySQL password is required");
            }
            config.setMysqlPassword(password);
            
            config.setMysqlTable((String) mysql.getOrDefault("table", "merged_api"));
        }

        // 解析规则引擎配置
        Map<String, Object> rule = (Map<String, Object>) data.get("rule");
        if (rule != null) {
            config.setRuleConfigPath((String) rule.getOrDefault("config-path", "rules.json"));
        }

        // 解析断点续传配置
        Map<String, Object> progress = (Map<String, Object>) data.get("progress");
        if (progress != null) {
            config.setProgressFile((String) progress.getOrDefault("file", "progress.txt"));
        }

        return config;
    }
}