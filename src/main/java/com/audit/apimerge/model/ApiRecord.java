package com.audit.apimerge.model;

/**
 * API记录（从ClickHouse读取）
 */
public class ApiRecord {
    private long id;          // 主键ID
    private String url;       // API URL
    private String method;    // HTTP方法（GET/POST等）
    private String name;      // API名称
    private long createTime;  // 创建时间
    private String app;       // 应用标识（如：order-system, user-center）

    public ApiRecord(long id, String url, String method, String name, long createTime) {
        this.id = id;
        this.url = url;
        this.method = method;
        this.name = name;
        this.createTime = createTime;
    }

    public ApiRecord(long id, String url, String method, String name, long createTime, String app) {
        this.id = id;
        this.url = url;
        this.method = method;
        this.name = name;
        this.createTime = createTime;
        this.app = app;
    }

    public long getId() { return id; }
    public String getUrl() { return url; }
    public String getMethod() { return method; }
    public String getName() { return name; }
    public long getCreateTime() { return createTime; }
    public String getApp() { return app; }
    public void setApp(String app) { this.app = app; }
}