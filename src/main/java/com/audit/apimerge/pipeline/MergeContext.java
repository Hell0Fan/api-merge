package com.audit.apimerge.pipeline;

import com.audit.apimerge.model.MergeResult;

public class MergeContext {
    private String originalUrl;
    private MergeResult ruleResult;
    private String defaultResult;
    private String finalResult;
    private String mergeType;

    public void determineFinal() {
        if (ruleResult != null && ruleResult.getMergedUrl() != null
            && !ruleResult.getMergedUrl().equals(originalUrl)) {
            finalResult = ruleResult.getMergedUrl();
            mergeType = "RULE";
        } else if (defaultResult != null && !defaultResult.equals(originalUrl)) {
            finalResult = defaultResult;
            mergeType = "DEFAULT";
        } else {
            mergeType = "NONE";
        }
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public MergeResult getRuleResult() {
        return ruleResult;
    }

    public void setRuleResult(MergeResult ruleResult) {
        this.ruleResult = ruleResult;
    }

    public String getDefaultResult() {
        return defaultResult;
    }

    public void setDefaultResult(String defaultResult) {
        this.defaultResult = defaultResult;
    }

    public String getFinalResult() {
        return finalResult;
    }

    public void setFinalResult(String finalResult) {
        this.finalResult = finalResult;
    }

    public String getMergeType() {
        return mergeType;
    }

    public void setMergeType(String mergeType) {
        this.mergeType = mergeType;
    }
}