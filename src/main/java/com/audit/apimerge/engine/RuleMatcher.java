package com.audit.apimerge.engine;

import com.audit.apimerge.model.MergeRule;
import com.audit.apimerge.model.MergeResult;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class RuleMatcher {

    private final List<MergeRule> rules;

    public RuleMatcher(List<MergeRule> rules) {
        this.rules = rules != null 
            ? rules.stream()
                .filter(MergeRule::isEnabled)
                .sorted(Comparator.comparingInt(MergeRule::getPriority))
                .collect(Collectors.toList())
            : Collections.emptyList();
    }

    public MergeResult match(String url) {
        for (MergeRule rule : rules) {
            MergeResult result = matchRule(url, rule);
            if (result != null) {
                return result;
            }
        }

        return new MergeResult(url, url, null, new HashMap<>());
    }

    private MergeResult matchRule(String url, MergeRule rule) {
        String type = rule.getType();
        
        if ("path_param".equals(type)) {
            PathParamMatcher matcher = new PathParamMatcher();
            return matcher.match(url, rule);
        } else if ("exact".equals(type)) {
            ExactMatcher matcher = new ExactMatcher();
            return matcher.match(url, rule);
        } else if ("prefix".equals(type)) {
            PrefixMatcher matcher = new PrefixMatcher();
            return matcher.match(url, rule);
        } else if ("regex".equals(type)) {
            RegexMatcher matcher = new RegexMatcher();
            return matcher.match(url, rule);
        }

        return null;
    }

    public List<MergeRule> getRules() {
        return rules;
    }
}
