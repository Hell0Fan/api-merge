package com.audit.apimerge.config;

import org.yaml.snakeyaml.Yaml;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

/**
 * 配置加载器
 * 从 YAML 文件加载应用配置
 */
public class ConfigLoader {

    /**
     * 从指定路径加载 YAML 配置
     * @param path 配置文件路径
     * @return AppConfig 实例
     */
    public static AppConfig load(String path) {
        Yaml yaml = new Yaml();
        try {
            Map<String, Object> data = yaml.load(new FileInputStream(path));
            return mapToConfig(data);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Config file not found: " + path, e);
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
            config.setClickhouseJdbcUrl((String) clickhouse.get("jdbc-url"));
            config.setClickhouseUsername((String) clickhouse.getOrDefault("username", "default"));
            config.setClickhousePassword((String) clickhouse.getOrDefault("password", ""));
            config.setBatchSize((Integer) clickhouse.getOrDefault("batch-size", 1000));
        }

        // 解析 MySQL 配置
        Map<String, Object> mysql = (Map<String, Object>) data.get("mysql");
        if (mysql != null) {
            config.setMysqlJdbcUrl((String) mysql.get("jdbc-url"));
            config.setMysqlUsername((String) mysql.get("username"));
            config.setMysqlPassword((String) mysql.get("password"));
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