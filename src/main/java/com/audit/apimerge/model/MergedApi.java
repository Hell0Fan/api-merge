package com.audit.apimerge.model;

/**
 * 合并后的API记录（写入MySQL）
 */
public class MergedApi {
    private Long id;               // 主键
    private String mergedUrl;      // 合并后的标准化URL
    private String originalUrls;   // 原始URL列表（JSON数组）
    private String method;          // HTTP方法
    private Integer sourceCount;   // 来源记录数
    private String mergeType;      // 合并类型：RULE/DEFAULT/NONE
    private Long createTime;       // 创建时间
    private String app;            // 应用标识

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMergedUrl() { return mergedUrl; }
    public void setMergedUrl(String mergedUrl) { this.mergedUrl = mergedUrl; }

    public String getOriginalUrls() { return originalUrls; }
    public void setOriginalUrls(String originalUrls) { this.originalUrls = originalUrls; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public Integer getSourceCount() { return sourceCount; }
    public void setSourceCount(Integer sourceCount) { this.sourceCount = sourceCount; }

    public String getMergeType() { return mergeType; }
    public void setMergeType(String mergeType) { this.mergeType = mergeType; }

    public Long getCreateTime() { return createTime; }
    public void setCreateTime(Long createTime) { this.createTime = createTime; }

    public String getApp() { return app; }
    public void setApp(String app) { this.app = app; }
}