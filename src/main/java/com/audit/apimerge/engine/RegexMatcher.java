package com.audit.apimerge.engine;

import com.audit.apimerge.model.MergeRule;
import com.audit.apimerge.model.MergeResult;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexMatcher {

    private Pattern compilePattern(String pattern) {
        return Pattern.compile("^" + pattern + "$");
    }

    public MergeResult match(String url, MergeRule rule) {
        String pattern = rule.getMatchPattern();
        
        if (pattern == null || url == null) {
            return null;
        }

        try {
            Pattern regex = compilePattern(pattern);
            Matcher matcher = regex.matcher(url);

            if (matcher.find()) {
                Map<String, String> params = new HashMap<>();

                if (rule.getExtractParams() != null) {
                    for (int i = 0; i < rule.getExtractParams().size(); i++) {
                        if (i + 1 <= matcher.groupCount()) {
                            params.put(rule.getExtractParams().get(i), matcher.group(i + 1));
                        }
                    }
                }

                String outputPattern = rule.getOutputPattern() != null 
                    ? rule.getOutputPattern() 
                    : url;

                return new MergeResult(outputPattern, url, rule, params);
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }
}
