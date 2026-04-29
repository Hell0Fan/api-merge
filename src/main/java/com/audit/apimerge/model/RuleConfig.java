package com.audit.apimerge.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class RuleConfig {

    @JsonProperty("rules")
    private List<MergeRule> rules;

    public RuleConfig() {
    }

    public RuleConfig(List<MergeRule> rules) {
        this.rules = rules;
    }

    public List<MergeRule> getRules() { return rules; }
    public void setRules(List<MergeRule> rules) { this.rules = rules; }
}
