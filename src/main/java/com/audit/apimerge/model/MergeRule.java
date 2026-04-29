package com.audit.apimerge.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class MergeRule {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private String type;

    @JsonProperty("priority")
    private Integer priority;

    @JsonProperty("matchPattern")
    private String matchPattern;

    @JsonProperty("extractParams")
    private List<String> extractParams;

    @JsonProperty("outputPattern")
    private String outputPattern;

    @JsonProperty("enabled")
    private boolean enabled;

    @JsonProperty("description")
    private String description;

    public MergeRule() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public String getMatchPattern() { return matchPattern; }
    public void setMatchPattern(String matchPattern) { this.matchPattern = matchPattern; }

    public List<String> getExtractParams() { return extractParams; }
    public void setExtractParams(List<String> extractParams) { this.extractParams = extractParams; }

    public String getOutputPattern() { return outputPattern; }
    public void setOutputPattern(String outputPattern) { this.outputPattern = outputPattern; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MergeRule mergeRule = (MergeRule) o;
        return id != null && id.equals(mergeRule.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
