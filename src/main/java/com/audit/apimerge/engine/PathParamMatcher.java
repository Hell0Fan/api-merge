package com.audit.apimerge.engine;

import com.audit.apimerge.model.MergeRule;
import com.audit.apimerge.model.MergeResult;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathParamMatcher {

    private static final Pattern NAMED_PARAM_PATTERN = Pattern.compile("\\{([^}]+)\\}");

    public MergeResult match(String url, MergeRule rule) {
        String pattern = rule.getMatchPattern();
        String outputPattern = rule.getOutputPattern();
        
        if (pattern == null || url == null) {
            return null;
        }

        String[] patternParts = pattern.split("/");
        String[] urlParts = url.split("/");

        if (patternParts.length != urlParts.length) {
            return null;
        }

        Map<String, String> params = new HashMap<>();
        boolean matched = true;
        int paramIndex = 1;

        for (int i = 0; i < patternParts.length; i++) {
            String part = patternParts[i];
            if ("*".equals(part)) {
                params.put("p" + paramIndex, urlParts[i]);
                paramIndex++;
            } else if (isNamedParam(part)) {
                String paramName = extractParamName(part);
                params.put(paramName, urlParts[i]);
            } else if (!part.equals(urlParts[i])) {
                matched = false;
                break;
            }
        }

        if (!matched) {
            return null;
        }

        if (rule.getExtractParams() != null) {
            int idx = 0;
            for (int i = 0; i < patternParts.length; i++) {
                String part = patternParts[i];
                if ("*".equals(part) && idx < rule.getExtractParams().size()) {
                    params.put(rule.getExtractParams().get(idx), urlParts[i]);
                    idx++;
                } else if (isNamedParam(part) && rule.getExtractParams().contains(extractParamName(part))) {
                    // Named param already extracted above
                }
            }
        }

        String mergedUrl = buildMergedUrl(pattern, urlParts, outputPattern);

        return new MergeResult(mergedUrl, url, rule, params);
    }

    private boolean isNamedParam(String part) {
        return part != null && part.startsWith("{") && part.endsWith("}");
    }

    private String extractParamName(String part) {
        if (isNamedParam(part)) {
            return part.substring(1, part.length() - 1);
        }
        return null;
    }

    private String buildMergedUrl(String pattern, String[] urlParts, String outputPattern) {
        if (outputPattern != null) {
            return outputPattern;
        }

        StringBuilder sb = new StringBuilder();
        String[] patternParts = pattern.split("/");
        int paramIndex = 1;

        for (int i = 0; i < patternParts.length; i++) {
            if (i > 0) {
                sb.append("/");
            }
            String part = patternParts[i];
            if ("*".equals(part)) {
                sb.append("{p").append(paramIndex).append("}");
                paramIndex++;
            } else if (isNamedParam(part)) {
                sb.append(part);
            } else {
                sb.append(part);
            }
        }

        return sb.toString();
    }
}
