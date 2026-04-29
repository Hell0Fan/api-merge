package com.audit.apimerge;

import com.audit.apimerge.engine.RuleMatcher;
import com.audit.apimerge.loader.RuleConfigLoader;
import com.audit.apimerge.model.MergeResult;
import com.audit.apimerge.model.MergeRule;
import com.audit.apimerge.model.RuleConfig;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ApiMergeEngine {

    private final RuleConfigLoader configLoader;
    private volatile RuleMatcher currentMatcher;

    public ApiMergeEngine(String configPath) {
        this.configLoader = new RuleConfigLoader(configPath);
        reload();
    }

    public void reload() {
        RuleConfig config = configLoader.load();
        List<MergeRule> rules = config.getRules();
        
        if (rules != null) {
            List<MergeRule> sortedRules = rules.stream()
                .filter(MergeRule::isEnabled)
                .sorted(Comparator.comparingInt(MergeRule::getPriority))
                .collect(Collectors.toList());
            this.currentMatcher = new RuleMatcher(sortedRules);
        } else {
            this.currentMatcher = new RuleMatcher(Collections.emptyList());
        }
    }

    public MergeResult merge(String url) {
        RuleMatcher matcher = this.currentMatcher;
        if (matcher != null) {
            return matcher.match(url);
        }
        return new MergeResult(url, url, null, new HashMap<>());
    }

    public RuleConfig getConfig() {
        return configLoader.getConfig();
    }

    public String getConfigPath() {
        return configLoader.getConfigPath();
    }
}
