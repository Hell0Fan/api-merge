package com.audit.apimerge.model;

import java.util.HashMap;
import java.util.Map;

public class MergeResult {

    private String mergedUrl;
    private String originalUrl;
    private MergeRule matchedRule;
    private Map<String, String> extractedParams;

    public MergeResult() {
    }

    public MergeResult(String mergedUrl, String originalUrl, 
                        MergeRule matchedRule, Map<String, String> extractedParams) {
        this.mergedUrl = mergedUrl;
        this.originalUrl = originalUrl;
        this.matchedRule = matchedRule;
        this.extractedParams = extractedParams != null ? extractedParams : new HashMap<>();
    }

    public String getMergedUrl() { return mergedUrl; }
    public void setMergedUrl(String mergedUrl) { this.mergedUrl = mergedUrl; }

    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }

    public MergeRule getMatchedRule() { return matchedRule; }
    public void setMatchedRule(MergeRule matchedRule) { this.matchedRule = matchedRule; }

    public Map<String, String> getExtractedParams() { return extractedParams; }
    public void setExtractedParams(Map<String, String> extractedParams) { this.extractedParams = extractedParams; }

    public Map<String, String> getDimensions() {
        return extractedParams;
    }
}
