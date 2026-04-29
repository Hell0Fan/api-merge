package com.audit.apimerge.engine;

import com.audit.apimerge.model.MergeRule;
import com.audit.apimerge.model.MergeResult;

import java.util.HashMap;

public class PrefixMatcher {

    public MergeResult match(String url, MergeRule rule) {
        String pattern = rule.getMatchPattern();
        
        if (pattern == null || url == null) {
            return null;
        }

        if (url.startsWith(pattern)) {
            String outputPattern = rule.getOutputPattern() != null 
                ? rule.getOutputPattern() 
                : pattern;
            return new MergeResult(outputPattern, url, rule, new HashMap<>());
        }

        return null;
    }
}
