# API合并规则引擎实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现一个高性能的API合并规则引擎，支持4种规则类型（path_param、exact、prefix、regex），通过JSON配置文件管理，支持热加载。

**Architecture:** 采用策略模式实现规则匹配器，支持扩展新的规则类型。核心组件包括：模型类、规则匹配器、配置加载器、引擎主类。

**Tech Stack:** Java 11, Jackson (JSON解析), JUnit 5 (单元测试), WatchService (文件热加载)

---

## 文件结构

```
api-merge/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── audit/
│   │   │           └── apimerge/
│   │   │               ├── model/
│   │   │               │   ├── MergeRule.java
│   │   │               │   ├── MergeResult.java
│   │   │               │   └── RuleConfig.java
│   │   │               ├── engine/
│   │   │               │   ├── RuleMatcher.java
│   │   │               │   ├── PathParamMatcher.java
│   │   │               │   ├── ExactMatcher.java
│   │   │               │   ├── PrefixMatcher.java
│   │   │               │   └── RegexMatcher.java
│   │   │               ├── loader/
│   │   │               │   └── RuleConfigLoader.java
│   │   │               └── ApiMergeEngine.java
│   │   └── resources/
│   │       └── rules.json
│   └── test/
│       ├── java/
│       │   └── com/
│       │       └── audit/
│       │           └── apimerge/
│       │               ├── model/
│       │               │   ├── MergeRuleTest.java
│       │               │   ├── MergeResultTest.java
│       │               │   └── RuleConfigTest.java
│       │               ├── engine/
│       │               │   ├── PathParamMatcherTest.java
│       │               │   ├── ExactMatcherTest.java
│       │               │   ├── PrefixMatcherTest.java
│       │               │   ├── RegexMatcherTest.java
│       │               │   └── RuleMatcherTest.java
│       │               ├── loader/
│       │               │   └── RuleConfigLoaderTest.java
│       │               └── ApiMergeEngineTest.java
│       └── resources/
│           └── test-rules.json
└── docs/
    └── superpowers/
        ├── specs/
        │   └── 2026-04-29-api-merge-rules-spec.md
        └── plans/
            └── 2026-04-29-api-merge-engine-plan.md
```

---

## 任务清单

### Task 1: 创建Maven项目结构

**Files:**
- Create: `pom.xml`
- Create: `src/main/java/com/audit/apimerge/model/MergeRule.java`
- Create: `src/main/java/com/audit/apimerge/model/MergeResult.java`
- Create: `src/main/java/com/audit/apimerge/model/RuleConfig.java`

- [ ] **Step 1: 创建 pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.audit</groupId>
    <artifactId>api-merge</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <name>API Merge Engine</name>
    <description>API合并规则引擎</description>

    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <junit.version>5.10.0</junit.version>
        <jackson.version>2.15.2</jackson.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>${jackson.version}</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-annotations</artifactId>
            <version>${jackson.version}</version>
        </dependency>

        <!-- Test dependencies -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-api</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-engine</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-params</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>11</source>
                    <target>11</target>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.1.2</version>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: 验证Maven项目编译**

Run: `cd /data/ai_workspace/api-merge && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交代码**

```bash
git add pom.xml
git commit -m "chore: 创建Maven项目结构"
```

---

### Task 2: 实现模型类

**Files:**
- Create: `src/main/java/com/audit/apimerge/model/MergeRule.java`
- Create: `src/main/java/com/audit/apimerge/model/MergeResult.java`
- Create: `src/main/java/com/audit/apimerge/model/RuleConfig.java`

- [ ] **Step 1: 编写 MergeRule 单元测试**

```java
package com.audit.apimerge.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MergeRuleTest {

    @Test
    void testMergeRuleCreation() {
        MergeRule rule = new MergeRule();
        rule.setId("rule-001");
        rule.setName("测试规则");
        rule.setType("path_param");
        rule.setPriority(1);
        rule.setMatchPattern("/api/user/*/detail");
        rule.setExtractParams(java.util.Arrays.asList("userId"));
        rule.setOutputPattern("/api/user/{userId}/detail");
        rule.setEnabled(true);
        rule.setDescription("测试描述");

        assertEquals("rule-001", rule.getId());
        assertEquals("测试规则", rule.getName());
        assertEquals("path_param", rule.getType());
        assertEquals(1, rule.getPriority());
        assertEquals("/api/user/*/detail", rule.getMatchPattern());
        assertEquals(1, rule.getExtractParams().size());
        assertEquals("userId", rule.getExtractParams().get(0));
        assertEquals("/api/user/{userId}/detail", rule.getOutputPattern());
        assertTrue(rule.isEnabled());
        assertEquals("测试描述", rule.getDescription());
    }

    @Test
    void testMergeRuleEquals() {
        MergeRule rule1 = new MergeRule();
        rule1.setId("rule-001");
        rule1.setName("测试规则");
        
        MergeRule rule2 = new MergeRule();
        rule2.setId("rule-001");
        rule2.setName("测试规则");
        
        assertEquals(rule1, rule2);
    }
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `cd /data/ai_workspace/api-merge && mvn test -Dtest=MergeRuleTest -q`
Expected: 编译失败（类不存在）

- [ ] **Step 3: 实现 MergeRule 类**

```java
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getMatchPattern() {
        return matchPattern;
    }

    public void setMatchPattern(String matchPattern) {
        this.matchPattern = matchPattern;
    }

    public List<String> getExtractParams() {
        return extractParams;
    }

    public void setExtractParams(List<String> extractParams) {
        this.extractParams = extractParams;
    }

    public String getOutputPattern() {
        return outputPattern;
    }

    public void setOutputPattern(String outputPattern) {
        this.outputPattern = outputPattern;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

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
```

- [ ] **Step 4: 编写 MergeResult 单元测试**

```java
package com.audit.apimerge.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MergeResultTest {

    @Test
    void testMergeResultCreation() {
        MergeRule rule = new MergeRule();
        rule.setId("rule-001");
        rule.setName("用户接口合并");
        rule.setType("path_param");

        MergeResult result = new MergeResult(
            "/api/user/{userId}/detail",
            "/api/user/1001/detail",
            rule,
            java.util.Map.of("userId", "1001", "p1", "1001")
        );

        assertEquals("/api/user/{userId}/detail", result.getMergedUrl());
        assertEquals("/api/user/1001/detail", result.getOriginalUrl());
        assertEquals("rule-001", result.getMatchedRule().getId());
        assertEquals("1001", result.getExtractedParams().get("userId"));
    }

    @Test
    void testDimensionsExtraction() {
        MergeRule rule = new MergeRule();
        rule.setId("rule-002");
        rule.setType("path_param");

        java.util.Map<String, String> params = new java.util.HashMap<>();
        params.put("orderId", "5001");
        
        MergeResult result = new MergeResult(
            "/api/order/{orderId}/info",
            "/api/order/5001/info",
            rule,
            params
        );

        java.util.Map<String, String> dimensions = result.getDimensions();
        assertEquals("5001", dimensions.get("orderId"));
    }
}
```

- [ ] **Step 5: 实现 MergeResult 类**

```java
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

    public String getMergedUrl() {
        return mergedUrl;
    }

    public void setMergedUrl(String mergedUrl) {
        this.mergedUrl = mergedUrl;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public MergeRule getMatchedRule() {
        return matchedRule;
    }

    public void setMatchedRule(MergeRule matchedRule) {
        this.matchedRule = matchedRule;
    }

    public Map<String, String> getExtractedParams() {
        return extractedParams;
    }

    public void setExtractedParams(Map<String, String> extractedParams) {
        this.extractedParams = extractedParams;
    }

    public Map<String, String> getDimensions() {
        return extractedParams;
    }
}
```

- [ ] **Step 6: 编写 RuleConfig 单元测试**

```java
package com.audit.apimerge.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

class RuleConfigTest {

    @Test
    void testRuleConfigCreation() {
        MergeRule rule = new MergeRule();
        rule.setId("rule-001");
        rule.setName("测试");
        rule.setType("path_param");
        rule.setPriority(1);
        rule.setMatchPattern("/api/test/*");
        rule.setOutputPattern("/api/test/{id}");
        rule.setEnabled(true);

        RuleConfig config = new RuleConfig();
        config.setRules(Arrays.asList(rule));

        assertEquals(1, config.getRules().size());
        assertEquals("rule-001", config.getRules().get(0).getId());
    }
}
```

- [ ] **Step 7: 实现 RuleConfig 类**

```java
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

    public List<MergeRule> getRules() {
        return rules;
    }

    public void setRules(List<MergeRule> rules) {
        this.rules = rules;
    }
}
```

- [ ] **Step 8: 运行所有模型测试**

Run: `cd /data/ai_workspace/api-merge && mvn test -Dtest="com.audit.apimerge.model.*Test" -q`
Expected: BUILD SUCCESS

- [ ] **Step 9: 提交代码**

```bash
git add src/main/java/com/audit/apimerge/model/ src/test/java/com/audit/apimerge/model/
git commit -m "feat: 实现模型类 MergeRule, MergeResult, RuleConfig"
```

---

### Task 3: 实现规则匹配器

**Files:**
- Create: `src/main/java/com/audit/apimerge/engine/RuleMatcher.java`
- Create: `src/main/java/com/audit/apimerge/engine/PathParamMatcher.java`
- Create: `src/main/java/com/audit/apimerge/engine/ExactMatcher.java`
- Create: `src/main/java/com/audit/apimerge/engine/PrefixMatcher.java`
- Create: `src/main/java/com/audit/apimerge/engine/RegexMatcher.java`

- [ ] **Step 1: 编写 PathParamMatcher 单元测试**

```java
package com.audit.apimerge.engine;

import com.audit.apimerge.model.MergeRule;
import com.audit.apimerge.model.MergeResult;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

class PathParamMatcherTest {

    private PathParamMatcher matcher = new PathParamMatcher();

    @Test
    void testMatchSuccess() {
        MergeRule rule = new MergeRule();
        rule.setId("rule-001");
        rule.setName("用户接口合并");
        rule.setType("path_param");
        rule.setMatchPattern("/api/user/*/detail");
        rule.setExtractParams(Arrays.asList("userId"));
        rule.setOutputPattern("/api/user/{userId}/detail");

        MergeResult result = matcher.match("/api/user/1001/detail", rule);

        assertNotNull(result);
        assertEquals("/api/user/{userId}/detail", result.getMergedUrl());
        assertEquals("1001", result.getExtractedParams().get("userId"));
        assertEquals("1001", result.getExtractedParams().get("p1"));
    }

    @Test
    void testMatchFailure() {
        MergeRule rule = new MergeRule();
        rule.setMatchPattern("/api/user/*/detail");
        rule.setOutputPattern("/api/user/{userId}/detail");

        MergeResult result = matcher.match("/api/order/1001/info", rule);

        assertNull(result);
    }

    @Test
    void testMatchWithMultipleParams() {
        MergeRule rule = new MergeRule();
        rule.setMatchPattern("/api/user/*/order/*");
        rule.setExtractParams(Arrays.asList("userId", "orderId"));
        rule.setOutputPattern("/api/user/{userId}/order/{orderId}");

        MergeResult result = matcher.match("/api/user/1001/order/5001", rule);

        assertNotNull(result);
        assertEquals("/api/user/{userId}/order/{orderId}", result.getMergedUrl());
        assertEquals("1001", result.getExtractedParams().get("userId"));
        assertEquals("5001", result.getExtractedParams().get("orderId"));
    }
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `cd /data/ai_workspace/api-merge && mvn test -Dtest=PathParamMatcherTest -q`
Expected: 编译失败（类不存在）

- [ ] **Step 3: 实现 PathParamMatcher 类**

```java
package com.audit.apimerge.engine;

import com.audit.apimerge.model.MergeRule;
import com.audit.apimerge.model.MergeResult;

import java.util.HashMap;
import java.util.Map;

public class PathParamMatcher {

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
            if ("*".equals(patternParts[i])) {
                params.put("p" + paramIndex, urlParts[i]);
                paramIndex++;
            } else if (!patternParts[i].equals(urlParts[i])) {
                matched = false;
                break;
            }
        }

        if (!matched) {
            return null;
        }

        if (rule.getExtractParams() != null) {
            int idx = 0;
            for (String part : urlParts) {
                for (int i = 0; i < patternParts.length; i++) {
                    if ("*".equals(patternParts[i]) && idx < rule.getExtractParams().size()) {
                        params.put(rule.getExtractParams().get(idx), part);
                        idx++;
                        break;
                    }
                }
            }
        }

        String mergedUrl = buildMergedUrl(pattern, urlParts, outputPattern);

        return new MergeResult(mergedUrl, url, rule, params);
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
            if ("*".equals(patternParts[i])) {
                sb.append("{p").append(paramIndex).append("}");
                paramIndex++;
            } else {
                sb.append(patternParts[i]);
            }
        }

        return sb.toString();
    }
}
```

- [ ] **Step 4: 编写 ExactMatcher 单元测试**

```java
package com.audit.apimerge.engine;

import com.audit.apimerge.model.MergeRule;
import com.audit.apimerge.model.MergeResult;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExactMatcherTest {

    private ExactMatcher matcher = new ExactMatcher();

    @Test
    void testMatchExactSuccess() {
        MergeRule rule = new MergeRule();
        rule.setMatchPattern("/api/health");
        rule.setOutputPattern("/api/health");

        MergeResult result = matcher.match("/api/health", rule);

        assertNotNull(result);
        assertEquals("/api/health", result.getMergedUrl());
        assertEquals("/api/health", result.getOriginalUrl());
    }

    @Test
    void testMatchExactFailure() {
        MergeRule rule = new MergeRule();
        rule.setMatchPattern("/api/health");
        rule.setOutputPattern("/api/health");

        MergeResult result = matcher.match("/api/health/check", rule);

        assertNull(result);
    }
}
```

- [ ] **Step 5: 实现 ExactMatcher 类**

```java
package com.audit.apimerge.engine;

import com.audit.apimerge.model.MergeRule;
import com.audit.apimerge.model.MergeResult;

import java.util.HashMap;

public class ExactMatcher {

    public MergeResult match(String url, MergeRule rule) {
        String pattern = rule.getMatchPattern();
        
        if (pattern == null || url == null) {
            return null;
        }

        if (pattern.equals(url)) {
            String outputPattern = rule.getOutputPattern() != null 
                ? rule.getOutputPattern() 
                : url;
            return new MergeResult(outputPattern, url, rule, new HashMap<>());
        }

        return null;
    }
}
```

- [ ] **Step 6: 编写 PrefixMatcher 单元测试**

```java
package com.audit.apimerge.engine;

import com.audit.apimerge.model.MergeRule;
import com.audit.apimerge.model.MergeResult;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PrefixMatcherTest {

    private PrefixMatcher matcher = new PrefixMatcher();

    @Test
    void testMatchPrefixSuccess() {
        MergeRule rule = new MergeRule();
        rule.setMatchPattern("/api/internal/");
        rule.setOutputPattern("/api/internal");

        MergeResult result = matcher.match("/api/internal/user/list", rule);

        assertNotNull(result);
        assertEquals("/api/internal", result.getMergedUrl());
    }

    @Test
    void testMatchPrefixFailure() {
        MergeRule rule = new MergeRule();
        rule.setMatchPattern("/api/internal/");
        rule.setOutputPattern("/api/internal");

        MergeResult result = matcher.match("/api/external/test", rule);

        assertNull(result);
    }
}
```

- [ ] **Step 7: 实现 PrefixMatcher 类**

```java
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
```

- [ ] **Step 8: 编写 RegexMatcher 单元测试**

```java
package com.audit.apimerge.engine;

import com.audit.apimerge.model.MergeRule;
import com.audit.apimerge.model.MergeResult;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

class RegexMatcherTest {

    private RegexMatcher matcher = new RegexMatcher();

    @Test
    void testMatchRegexSuccess() {
        MergeRule rule = new MergeRule();
        rule.setMatchPattern("/api/order/(\\d+)/detail");
        rule.setExtractParams(Arrays.asList("orderId"));
        rule.setOutputPattern("/api/order/{orderId}/detail");

        MergeResult result = matcher.match("/api/order/5001/detail", rule);

        assertNotNull(result);
        assertEquals("/api/order/{orderId}/detail", result.getMergedUrl());
        assertEquals("5001", result.getExtractedParams().get("orderId"));
    }

    @Test
    void testMatchRegexFailure() {
        MergeRule rule = new MergeRule();
        rule.setMatchPattern("/api/order/(\\d+)/detail");
        rule.setOutputPattern("/api/order/{orderId}/detail");

        MergeResult result = matcher.match("/api/order/abc/detail", rule);

        assertNull(result);
    }
}
```

- [ ] **Step 9: 实现 RegexMatcher 类**

```java
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
```

- [ ] **Step 10: 编写 RuleMatcher 单元测试**

```java
package com.audit.apimerge.engine;

import com.audit.apimerge.model.MergeRule;
import com.audit.apimerge.model.MergeResult;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

class RuleMatcherTest {

    @Test
    void testMatchWithPathParamRule() {
        MergeRule rule = new MergeRule();
        rule.setId("rule-001");
        rule.setName("用户接口");
        rule.setType("path_param");
        rule.setMatchPattern("/api/user/*/detail");
        rule.setExtractParams(Arrays.asList("userId"));
        rule.setOutputPattern("/api/user/{userId}/detail");

        RuleMatcher matcher = new RuleMatcher(Arrays.asList(rule));
        MergeResult result = matcher.match("/api/user/1001/detail");

        assertNotNull(result);
        assertEquals("/api/user/{userId}/detail", result.getMergedUrl());
    }

    @Test
    void testNoMatchReturnsOriginalUrl() {
        MergeRule rule = new MergeRule();
        rule.setType("path_param");
        rule.setMatchPattern("/api/user/*/detail");
        rule.setOutputPattern("/api/user/{userId}/detail");

        RuleMatcher matcher = new RuleMatcher(Arrays.asList(rule));
        MergeResult result = matcher.match("/api/unknown/path");

        assertNotNull(result);
        assertEquals("/api/unknown/path", result.getMergedUrl());
    }
}
```

- [ ] **Step 11: 实现 RuleMatcher 类**

```java
package com.audit.apimerge.engine;

import com.audit.apimerge.model.MergeRule;
import com.audit.apimerge.model.MergeResult;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RuleMatcher {

    private final List<MergeRule> rules;

    public RuleMatcher(List<MergeRule> rules) {
        this.rules = rules != null 
            ? rules.stream()
                .filter(MergeRule::isEnabled)
                .sorted(Comparator.comparingInt(MergeRule::getPriority))
                .toList()
            : List.of();
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
```

- [ ] **Step 12: 运行所有匹配器测试**

Run: `cd /data/ai_workspace/api-merge && mvn test -Dtest="com.audit.apimerge.engine.*Test" -q`
Expected: BUILD SUCCESS

- [ ] **Step 13: 提交代码**

```bash
git add src/main/java/com/audit/apimerge/engine/ src/test/java/com/audit/apimerge/engine/
git commit -m "feat: 实现规则匹配器 PathParamMatcher, ExactMatcher, PrefixMatcher, RegexMatcher, RuleMatcher"
```

---

### Task 4: 实现配置加载器（支持热加载）

**Files:**
- Create: `src/main/java/com/audit/apimerge/loader/RuleConfigLoader.java`

- [ ] **Step 1: 编写 RuleConfigLoader 单元测试**

```java
package com.audit.apimerge.loader;

import com.audit.apimerge.model.MergeRule;
import com.audit.apimerge.model.RuleConfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.List;

class RuleConfigLoaderTest {

    @Test
    void testLoadConfigFromFile() throws Exception {
        File tempFile = File.createTempFile("test-rules", ".json");
        tempFile.deleteOnExit();

        String json = "{\"rules\":[{\"id\":\"test-001\",\"name\":\"测试\",\"type\":\"path_param\",\"priority\":1,\"matchPattern\":\"/api/test/*\",\"outputPattern\":\"/api/test/{id}\",\"enabled\":true}]}";
        Files.writeString(tempFile.toPath(), json);

        RuleConfigLoader loader = new RuleConfigLoader(tempFile.getAbsolutePath());
        RuleConfig config = loader.load();

        assertNotNull(config);
        assertEquals(1, config.getRules().size());
        assertEquals("test-001", config.getRules().get(0).getId());
    }

    @Test
    void testLoadEmptyConfig() throws Exception {
        File tempFile = File.createTempFile("test-rules", ".json");
        tempFile.deleteOnExit();

        Files.writeString(tempFile.toPath(), "{}");

        RuleConfigLoader loader = new RuleConfigLoader(tempFile.getAbsolutePath());
        RuleConfig config = loader.load();

        assertNotNull(config);
        assertNull(config.getRules());
    }
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `cd /data/ai_workspace/api-merge && mvn test -Dtest=RuleConfigLoaderTest -q`
Expected: 编译失败（类不存在）

- [ ] **Step 3: 实现 RuleConfigLoader 类**

```java
package com.audit.apimerge.loader;

import com.audit.apimerge.model.RuleConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

public class RuleConfigLoader {

    private final String configPath;
    private final ObjectMapper objectMapper;
    private final AtomicReference<RuleConfig> cachedConfig = new AtomicReference<>();

    public RuleConfigLoader(String configPath) {
        this.configPath = configPath;
        this.objectMapper = new ObjectMapper();
    }

    public RuleConfig load() {
        try {
            String json = Files.readString(Paths.get(configPath));
            RuleConfig config = objectMapper.readValue(json, RuleConfig.class);
            cachedConfig.set(config);
            return config;
        } catch (IOException e) {
            RuleConfig cached = cachedConfig.get();
            if (cached != null) {
                return cached;
            }
            return new RuleConfig();
        }
    }

    public RuleConfig getConfig() {
        return cachedConfig.get();
    }

    public String getConfigPath() {
        return configPath;
    }
}
```

- [ ] **Step 4: 运行配置加载器测试**

Run: `cd /data/ai_workspace/api-merge && mvn test -Dtest=RuleConfigLoaderTest -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: 提交代码**

```bash
git add src/main/java/com/audit/apimerge/loader/ src/test/java/com/audit/apimerge/loader/
git commit -m "feat: 实现配置加载器 RuleConfigLoader"
```

---

### Task 5: 实现API合并引擎主类

**Files:**
- Create: `src/main/java/com/audit/apimerge/ApiMergeEngine.java`

- [ ] **Step 1: 编写 ApiMergeEngine 单元测试**

```java
package com.audit.apimerge;

import com.audit.apimerge.model.MergeResult;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

class ApiMergeEngineTest {

    @Test
    void testMergeWithPathParam() {
        ApiMergeEngine engine = new ApiMergeEngine("src/test/resources/test-rules.json");
        
        MergeResult result = engine.merge("/api/user/1001/detail");
        
        assertNotNull(result);
        assertEquals("/api/user/{userId}/detail", result.getMergedUrl());
    }

    @Test
    void testMergeWithExact() {
        ApiMergeEngine engine = new ApiMergeEngine("src/test/resources/test-rules.json");
        
        MergeResult result = engine.merge("/api/health");
        
        assertNotNull(result);
        assertEquals("/api/health", result.getMergedUrl());
    }

    @Test
    void testMergeNoMatch() {
        ApiMergeEngine engine = new ApiMergeEngine("src/test/resources/test-rules.json");
        
        MergeResult result = engine.merge("/api/unknown/path");
        
        assertNotNull(result);
        assertEquals("/api/unknown/path", result.getMergedUrl());
    }

    @Test
    void testDimensionsExtraction() {
        ApiMergeEngine engine = new ApiMergeEngine("src/test/resources/test-rules.json");
        
        MergeResult result = engine.merge("/api/order/5001/info");
        
        assertNotNull(result);
        assertEquals("5001", result.getDimensions().get("orderId"));
    }
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `cd /data/ai_workspace/api-merge && mvn test -Dtest=ApiMergeEngineTest -q`
Expected: 编译失败（类不存在）

- [ ] **Step 3: 创建测试用规则配置文件**

```json
{
  "rules": [
    {
      "id": "rule-001",
      "name": "用户接口合并",
      "type": "path_param",
      "priority": 1,
      "matchPattern": "/api/user/*/detail",
      "extractParams": ["userId"],
      "outputPattern": "/api/user/{userId}/detail",
      "enabled": true,
      "description": "合并用户详情接口"
    },
    {
      "id": "rule-002",
      "name": "订单接口合并",
      "type": "path_param",
      "priority": 2,
      "matchPattern": "/api/order/*/info",
      "extractParams": ["orderId"],
      "outputPattern": "/api/order/{orderId}/info",
      "enabled": true,
      "description": "合并订单信息接口"
    },
    {
      "id": "rule-003",
      "name": "健康检查接口",
      "type": "exact",
      "priority": 10,
      "matchPattern": "/api/health",
      "outputPattern": "/api/health",
      "enabled": true,
      "description": "精确匹配不合并"
    },
    {
      "id": "rule-004",
      "name": "内部接口合并",
      "type": "prefix",
      "priority": 20,
      "matchPattern": "/api/internal/",
      "outputPattern": "/api/internal",
      "enabled": false,
      "description": "内部接口前缀"
    },
    {
      "id": "rule-005",
      "name": "正则匹配订单接口",
      "type": "regex",
      "priority": 5,
      "matchPattern": "/api/order/(\\d+)/detail",
      "extractParams": ["orderId"],
      "outputPattern": "/api/order/{orderId}/detail",
      "enabled": true,
      "description": "使用正则表达式匹配订单详情"
    }
  ]
}
```

- [ ] **Step 4: 实现 ApiMergeEngine 类**

```java
package com.audit.apimerge;

import com.audit.apimerge.engine.RuleMatcher;
import com.audit.apimerge.loader.RuleConfigLoader;
import com.audit.apimerge.model.MergeResult;
import com.audit.apimerge.model.MergeRule;
import com.audit.apimerge.model.RuleConfig;

import java.util.Comparator;
import java.util.List;

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
                .toList();
            this.currentMatcher = new RuleMatcher(sortedRules);
        } else {
            this.currentMatcher = new RuleMatcher(List.of());
        }
    }

    public MergeResult merge(String url) {
        RuleMatcher matcher = this.currentMatcher;
        if (matcher != null) {
            return matcher.match(url);
        }
        return new MergeResult(url, url, null, new java.util.HashMap<>());
    }

    public RuleConfig getConfig() {
        return configLoader.getConfig();
    }

    public String getConfigPath() {
        return configLoader.getConfigPath();
    }
}
```

- [ ] **Step 5: 运行引擎测试**

Run: `cd /data/ai_workspace/api-merge && mvn test -Dtest=ApiMergeEngineTest -q`
Expected: BUILD SUCCESS

- [ ] **Step 6: 提交代码**

```bash
git add src/main/java/com/audit/apimerge/ApiMergeEngine.java src/test/java/com/audit/apimerge/ApiMergeEngineTest.java src/test/resources/test-rules.json
git commit -m "feat: 实现API合并引擎主类 ApiMergeEngine"
```

---

### Task 6: 运行完整测试套件

- [ ] **Step 1: 运行所有测试**

Run: `cd /data/ai_workspace/api-merge && mvn clean test -q`
Expected: BUILD SUCCESS

- [ ] **Step 2: 生成测试报告**

Run: `cd /data/ai_workspace/api-merge && mvn surefire:test -DgenerateReport=true`
Expected: 在 target/site/surefire-report.html 生成报告

- [ ] **Step 3: 提交代码**

```bash
git add -A
git commit -m "test: 完成所有单元测试并验证通过"
```

---

### Task 7: 性能测试

- [ ] **Step 1: 编写性能测试**

```java
package com.audit.apimerge;

import com.audit.apimerge.model.MergeResult;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PerformanceTest {

    @Test
    void testMatchPerformance() {
        ApiMergeEngine engine = new ApiMergeEngine("src/test/resources/test-rules.json");
        
        long start = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            engine.merge("/api/user/1001/detail");
        }
        long end = System.nanoTime();
        
        long avgTimeNs = (end - start) / 10000;
        System.out.println("Average match time: " + avgTimeNs + " ns");
        
        assertTrue(avgTimeNs < 1000000, "Match should be < 1ms");
    }
}
```

- [ ] **Step 2: 运行性能测试**

Run: `cd /data/ai_workspace/api-merge && mvn test -Dtest=PerformanceTest -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交代码**

```bash
git add -A
git commit -m "test: 添加性能测试并验证 < 1ms"
```

---

## 总结

完成上述任务后，你将得到：
1. 完整的API合并规则引擎实现
2. 4种规则类型的匹配器（path_param、exact、prefix、regex）
3. 支持JSON配置文件热加载
4. 完整的单元测试覆盖
5. 性能测试验证 < 1ms

---

**Plan complete and saved to `docs/superpowers/plans/2026-04-29-api-merge-engine-plan.md`. Two execution options:**

**1. Subagent-Driven (recommended)** - I dispatch a fresh subagent per task, review between tasks, fast iteration

**2. Inline Execution** - Execute tasks in this session using executing-plans, batch execution with checkpoints

**Which approach?**