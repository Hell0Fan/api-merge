package com.audit.apimerge.config;

/**
 * 应用配置类
 * 包含 ClickHouse、MySQL、规则引擎、断点续传等配置项
 */
public class AppConfig {
    private String clickhouseJdbcUrl;
    private String clickhouseUsername = "default";
    private String clickhousePassword = "";
    private int batchSize = 1000;

    private String mysqlJdbcUrl;
    private String mysqlUsername;
    private String mysqlPassword;
    private String mysqlTable = "merged_api";

    private String ruleConfigPath = "rules.json";
    private String progressFile = "progress.txt";

    public String getClickhouseJdbcUrl() {
        return clickhouseJdbcUrl;
    }

    public void setClickhouseJdbcUrl(String clickhouseJdbcUrl) {
        this.clickhouseJdbcUrl = clickhouseJdbcUrl;
    }

    public String getClickhouseUsername() {
        return clickhouseUsername;
    }

    public void setClickhouseUsername(String clickhouseUsername) {
        this.clickhouseUsername = clickhouseUsername;
    }

    public String getClickhousePassword() {
        return clickhousePassword;
    }

    public void setClickhousePassword(String clickhousePassword) {
        this.clickhousePassword = clickhousePassword;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public String getMysqlJdbcUrl() {
        return mysqlJdbcUrl;
    }

    public void setMysqlJdbcUrl(String mysqlJdbcUrl) {
        this.mysqlJdbcUrl = mysqlJdbcUrl;
    }

    public String getMysqlUsername() {
        return mysqlUsername;
    }

    public void setMysqlUsername(String mysqlUsername) {
        this.mysqlUsername = mysqlUsername;
    }

    public String getMysqlPassword() {
        return mysqlPassword;
    }

    public void setMysqlPassword(String mysqlPassword) {
        this.mysqlPassword = mysqlPassword;
    }

    public String getMysqlTable() {
        return mysqlTable;
    }

    public void setMysqlTable(String mysqlTable) {
        this.mysqlTable = mysqlTable;
    }

    public String getRuleConfigPath() {
        return ruleConfigPath;
    }

    public void setRuleConfigPath(String ruleConfigPath) {
        this.ruleConfigPath = ruleConfigPath;
    }

    public String getProgressFile() {
        return progressFile;
    }

    public void setProgressFile(String progressFile) {
        this.progressFile = progressFile;
    }
}