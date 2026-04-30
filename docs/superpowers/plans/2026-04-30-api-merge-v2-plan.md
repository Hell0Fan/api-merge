# API Merge V2.0 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现V2.0：从ClickHouse读取API数据，自动合并后写入MySQL，支持持续运行和断点续传

**Architecture:** 主循环模式：查询ClickHouse → 内置规则+默认算法匹配 → 写入MySQL → 更新进度

**Tech Stack:** Java 11, Maven, ClickHouse JDBC, MySQL JDBC, JUnit 5

---

## 文件结构

```
src/main/java/com/audit/apimerge/
├── ApiMergeEngine.java              # 已存在，V1.0入口
├── pipeline/
│   ├── MergePipeline.java           # 主流程编排
│   ├── MergeContext.java            # 合并上下文
│   └── DefaultMerger.java          # 默认合并算法
├── reader/
│   └── ClickHouseReader.java        # ClickHouse读取
├── writer/
│   └── MySqlWriter.java             # MySQL写入
├── progress/
│   └── ProgressManager.java        # 断点续传管理
├── config/
│   └── AppConfig.java               # 配置类
└── App.java                         # 启动入口

src/test/
├── MergePipelineTest.java
├── DefaultMergerTest.java
└── ProgressManagerTest.java
```

---

## Task 1: 添加依赖

**Files:**
- Modify: `pom.xml`

- [ ] **Step 1: 添加 ClickHouse 和 MySQL 依赖**

```xml
<dependency>
    <groupId>ru.yandex.clickhouse</groupId>
    <artifactId>clickhouse-jdbc</artifactId>
    <version>0.3.2</version>
</dependency>
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.0.33</version>
</dependency>
```

- [ ] **Step 2: 验证编译**

Run: `mvn clean compile`
Expected: BUILD SUCCESS

---

## Task 2: 配置类

**Files:**
- Create: `src/main/java/com/audit/apimerge/config/AppConfig.java`

- [ ] **Step 1: 创建配置类**

```java
package com.audit.apimerge.config;

public class AppConfig {
    private String clickhouseJdbcUrl;
    private String clickhouseUsername = "default";
    private String clickhousePassword = "";
    private int batchSize = 1000;

    private String mysqlJdbcUrl;
    private String mysqlUsername;
    private String mysqlPassword;
    private String mysqlTable = "merged_api";

    private String ruleConfigPath = "rules.json";
    private String progressFile = "progress.txt";

    // getters and setters
}
```

- [ ] **Step 2: 添加 YAML 解析依赖到 pom.xml**

```xml
<dependency>
    <groupId>org.yaml</groupId>
    <artifactId>snakeyaml</artifactId>
    <version>2.0</version>
</dependency>
```

- [ ] **Step 3: 创建配置加载器 ConfigLoader.java**

```java
package com.audit.apimerge.config;

import java.io.FileInputStream;
import java.util.Properties;

public class ConfigLoader {
    public static AppConfig load(String path) {
        // 读取 properties 文件或 YAML
        // 返回 AppConfig 实例
    }
}
```

---

## Task 3: 断点续传

**Files:**
- Create: `src/main/java/com/audit/apimerge/progress/ProgressManager.java`
- Create: `src/test/java/com/audit/apimerge/progress/ProgressManagerTest.java`

- [ ] **Step 1: 创建测试**

```java
package com.audit.apimerge.progress;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProgressManagerTest {
    @Test
    void shouldReadLastProcessedId() {
        ProgressManager pm = new ProgressManager("test-progress.txt");
        // 测试读取
    }

    @Test
    void shouldSaveLastProcessedId() {
        ProgressManager pm = new ProgressManager("test-progress.txt");
        pm.save(12345);
        assertEquals(12345, pm.load());
    }
}
```

- [ ] **Step 2: 创建实现**

```java
package com.audit.apimerge.progress;

import java.io.*;

public class ProgressManager {
    private final String filePath;

    public ProgressManager(String filePath) {
        this.filePath = filePath;
    }

    public long load() {
        File file = new File(filePath);
        if (!file.exists()) return 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            if (line != null && line.startsWith("last_processed_id=")) {
                return Long.parseLong(line.split("=")[1]);
            }
        } catch (Exception e) {
            return 0;
        }
        return 0;
    }

    public void save(long id) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            pw.println("last_processed_id=" + id);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save progress", e);
        }
    }
}
```

- [ ] **Step 3: 运行测试**

Run: `mvn test -Dtest=ProgressManagerTest`
Expected: PASS

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/audit/apimerge/progress/ src/test/java/com/audit/apimerge/progress/ pom.xml
git commit -m "feat: add progress manager for checkpoint"
```

---

## Task 4: ClickHouse 读取

**Files:**
- Create: `src/main/java/com/audit/apimerge/reader/ClickHouseReader.java`
- Create: `src/main/java/com/audit/apimerge/model/ApiRecord.java`
- Create: `src/test/java/com/audit/apimerge/reader/ClickHouseReaderTest.java`

- [ ] **Step 1: 创建 ApiRecord 模型**

```java
package com.audit.apimerge.model;

public class ApiRecord {
    private long id;
    private String url;
    private String method;
    private String name;
    private long createTime;

    public ApiRecord(long id, String url, String method, String name, long createTime) {
        this.id = id;
        this.url = url;
        this.method = method;
        this.name = name;
        this.createTime = createTime;
    }

    // getters
}
```

- [ ] **Step 2: 创建测试**

```java
package com.audit.apimerge.reader;

import com.audit.apimerge.model.ApiRecord;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClickHouseReaderTest {
    @Test
    void shouldQueryWithLastId() {
        ClickHouseReader reader = new ClickHouseReader("jdbc:clickhouse://localhost:8123/test", "default", "", 100);
        // 模拟查询
    }
}
```

- [ ] **Step 3: 创建实现**

```java
package com.audit.apimerge.reader;

import com.audit.apimerge.model.ApiRecord;
import ru.yandex.clickhouse.ClickHouseDriver;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClickHouseReader {
    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final int batchSize;

    public ClickHouseReader(String jdbcUrl, String username, String password, int batchSize) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.batchSize = batchSize;
    }

    public List<ApiRecord> queryAfterId(long lastProcessedId) {
        List<ApiRecord> records = new ArrayList<>();
        String sql = "SELECT id, url, method, name, create_time FROM audit_record " +
                     "WHERE id > ? ORDER BY id LIMIT ?";

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, lastProcessedId);
            ps.setInt(2, batchSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(new ApiRecord(
                        rs.getLong("id"),
                        rs.getString("url"),
                        rs.getString("method"),
                        rs.getString("name"),
                        rs.getTimestamp("create_time").getTime()
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query ClickHouse", e);
        }
        return records;
    }
}
```

- [ ] **Step 4: 运行测试**

Run: `mvn test -Dtest=ClickHouseReaderTest`
Expected: PASS (mock模式或跳过)

- [ ] **Step 5: 提交**

```bash
git add src/main/java/com/audit/apimerge/reader/ src/main/java/com/audit/apimerge/model/ApiRecord.java src/test/
git commit -m "feat: add ClickHouse reader"
```

---

## Task 5: 默认合并算法

**Files:**
- Create: `src/main/java/com/audit/apimerge/pipeline/DefaultMerger.java`
- Create: `src/test/java/com/audit/apimerge/pipeline/DefaultMergerTest.java`

- [ ] **Step 1: 创建测试**

```java
package com.audit.apimerge.pipeline;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DefaultMergerTest {
    @Test
    void shouldNormalizeNumericPathParam() {
        DefaultMerger merger = new DefaultMerger();
        assertEquals("/api/user/{p}/detail", merger.normalize("/api/user/123/detail"));
    }

    @Test
    void shouldNormalizeAlphanumericPathParam() {
        DefaultMerger merger = new DefaultMerger();
        assertEquals("/api/user/{p}/detail", merger.normalize("/api/user/abc123/detail"));
    }

    @Test
    void shouldNormalizeMultipleParams() {
        DefaultMerger merger = new DefaultMerger();
        assertEquals("/api/{p}/{p}/items", merger.normalize("/api/123/456/items"));
    }
}
```

- [ ] **Step 2: 创建实现**

```java
package com.audit.apimerge.pipeline;

import java.util.regex.Pattern;

public class DefaultMerger {
    // 匹配纯数字
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("/(\\d+)(/|$)");
    // 匹配字母数字混合（长度>=4）
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("/([a-zA-Z0-9]{4,})(/|$)");

    public String normalize(String url) {
        String result = url;
        result = NUMERIC_PATTERN.matcher(result).replaceAll("/{$1}$2");
        result = ALPHANUMERIC_PATTERN.matcher(result).replaceAll("/{$1}$2");
        return result;
    }

    public String merge(String url) {
        return normalize(url);
    }
}
```

- [ ] **Step 3: 运行测试**

Run: `mvn test -Dtest=DefaultMergerTest`
Expected: PASS

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/audit/apimerge/pipeline/DefaultMerger.java src/test/java/com/audit/apimerge/pipeline/DefaultMergerTest.java
git commit -m "feat: add default merger algorithm"
```

---

## Task 6: MySQL 写入

**Files:**
- Create: `src/main/java/com/audit/apimerge/writer/MySqlWriter.java`
- Create: `src/main/java/com/audit/apimerge/model/MergedApi.java`
- Create: `src/test/java/com/audit/apimerge/writer/MySqlWriterTest.java`

- [ ] **Step 1: 创建 MergedApi 模型**

```java
package com.audit.apimerge.model;

public class MergedApi {
    private Long id;
    private String mergedUrl;
    private String originalUrls; // JSON array
    private String method;
    private Integer sourceCount;
    private String mergeType; // RULE / DEFAULT
    private Long createTime;

    // constructors, getters, setters
}
```

- [ ] **Step 2: 创建测试**

```java
package com.audit.apimerge.writer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MySqlWriterTest {
    @Test
    void shouldConstructInsertSql() {
        MySqlWriter writer = new MySqlWriter("jdbc:mysql://localhost:3306/test", "root", "pass", "merged_api");
        // 测试 SQL 构建
    }
}
```

- [ ] **Step 3: 创建实现**

```java
package com.audit.apimerge.writer;

import com.audit.apimerge.model.MergedApi;
import java.sql.*;
import java.util.List;

public class MySqlWriter {
    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final String tableName;

    public MySqlWriter(String jdbcUrl, String username, String password, String tableName) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.tableName = tableName;
    }

    public void write(List<MergedApi> apis) {
        String sql = "INSERT INTO " + tableName +
            " (merged_url, original_urls, method, source_count, merge_type, create_time) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (MergedApi api : apis) {
                ps.setString(1, api.getMergedUrl());
                ps.setString(2, api.getOriginalUrls());
                ps.setString(3, api.getMethod());
                ps.setInt(4, api.getSourceCount());
                ps.setString(5, api.getMergeType());
                ps.setLong(6, api.getCreateTime());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to write to MySQL", e);
        }
    }
}
```

- [ ] **Step 4: 运行测试**

Run: `mvn test -Dtest=MySqlWriterTest`
Expected: PASS

- [ ] **Step 5: 提交**

```bash
git add src/main/java/com/audit/apimerge/writer/ src/main/java/com/audit/apimerge/model/MergedApi.java src/test/
git commit -m "feat: add MySQL writer"
```

---

## Task 7: 合并上下文与主流程

**Files:**
- Create: `src/main/java/com/audit/apimerge/pipeline/MergeContext.java`
- Modify: `src/main/java/com/audit/apimerge/ApiMergeEngine.java` - 添加默认算法支持

- [ ] **Step 1: 创建 MergeContext**

```java
package com.audit.apimerge.pipeline;

import com.audit.apimerge.model.MergeResult;
import java.util.HashMap;
import java.util.Map;

public class MergeContext {
    private String originalUrl;
    private MergeResult ruleResult;
    private String defaultResult;
    private String finalResult;
    private String mergeType; // RULE / DEFAULT / NONE

    public void determineFinal() {
        if (ruleResult != null && ruleResult.getMergedUrl() != null) {
            finalResult = ruleResult.getMergedUrl();
            mergeType = "RULE";
        } else if (defaultResult != null) {
            finalResult = defaultResult;
            mergeType = "DEFAULT";
        } else {
            mergeType = "NONE";
        }
    }

    // getters, setters
}
```

- [ ] **Step 2: 修改 ApiMergeEngine 添加默认算法**

```java
// 在 ApiMergeEngine.java 中添加
private DefaultMerger defaultMerger;

public MergeResult mergeWithDefault(String url) {
    MergeResult ruleResult = merge(url);
    String defaultResult = defaultMerger.merge(url);

    MergeContext context = new MergeContext();
    context.setOriginalUrl(url);
    context.setRuleResult(ruleResult);
    context.setDefaultResult(defaultResult);
    context.determineFinal();

    return new MergeResult(url, context.getFinalResult(), context.getMergeType(), ruleResult.getExtractedParams());
}
```

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/audit/apimerge/pipeline/MergeContext.java src/main/java/com/audit/apimerge/ApiMergeEngine.java
git commit -m "feat: add default merger integration"
```

---

## Task 8: 主流程编排

**Files:**
- Create: `src/main/java/com/audit/apimerge/pipeline/MergePipeline.java`

- [ ] **Step 1: 创建 MergePipeline**

```java
package com.audit.apimerge.pipeline;

import com.audit.apimerge.config.AppConfig;
import com.audit.apimerge.model.ApiRecord;
import com.audit.apimerge.model.MergedApi;
import com.audit.apimerge.reader.ClickHouseReader;
import com.audit.apimerge.writer.MySqlWriter;
import com.audit.apimerge.progress.ProgressManager;
import com.audit.apimerge.ApiMergeEngine;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collectors;

public class MergePipeline {
    private final ClickHouseReader reader;
    private final MySqlWriter writer;
    private final ProgressManager progressManager;
    private final ApiMergeEngine engine;
    private final DefaultMerger defaultMerger;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MergePipeline(AppConfig config) {
        this.reader = new ClickHouseReader(
            config.getClickhouseJdbcUrl(),
            config.getClickhouseUsername(),
            config.getClickhousePassword(),
            config.getBatchSize()
        );
        this.writer = new MySqlWriter(
            config.getMysqlJdbcUrl(),
            config.getMysqlUsername(),
            config.getMysqlPassword(),
            config.getMysqlTable()
        );
        this.progressManager = new ProgressManager(config.getProgressFile());
        this.engine = new ApiMergeEngine(config.getRuleConfigPath());
        this.defaultMerger = new DefaultMerger();
    }

    public void run() {
        long lastProcessedId = progressManager.load();
        System.out.println("Starting from id: " + lastProcessedId);

        while (true) {
            List<ApiRecord> records = reader.queryAfterId(lastProcessedId);

            if (records.isEmpty()) {
                try {
                    Thread.sleep(5000); // 无数据时等待5秒
                } catch (InterruptedException e) {
                    break;
                }
                continue;
            }

            // 按 normalized URL 分组
            Map<String, List<ApiRecord>> grouped = groupByNormalized(records);

            // 写入合并结果
            List<MergedApi> mergedApis = buildMergedApis(grouped);
            if (!mergedApis.isEmpty()) {
                writer.write(mergedApis);
            }

            // 更新进度
            lastProcessedId = records.get(records.size() - 1).getId();
            progressManager.save(lastProcessedId);

            System.out.println("Processed " + records.size() + " records, progress: " + lastProcessedId);
        }
    }

    private Map<String, List<ApiRecord>> groupByNormalized(List<ApiRecord> records) {
        Map<String, List<ApiRecord>> grouped = new HashMap<>();
        for (ApiRecord record : records) {
            String normalized = normalize(record.getUrl());
            grouped.computeIfAbsent(normalized, k -> new ArrayList<>()).add(record);
        }
        return grouped;
    }

    private String normalize(String url) {
        // 优先用引擎匹配，无匹配则用默认算法
        var result = engine.merge(url);
        if (result.getMergedUrl() != null && !result.getMergedUrl().equals(url)) {
            return result.getMergedUrl();
        }
        return defaultMerger.merge(url);
    }

    private List<MergedApi> buildMergedApis(Map<String, List<ApiRecord>> grouped) {
        List<MergedApi> result = new ArrayList<>();
        long now = System.currentTimeMillis();

        for (Map.Entry<String, List<ApiRecord>> entry : grouped.entrySet()) {
            if (entry.getValue().size() < 2) continue; // 至少2条才合并

            List<String> urls = entry.getValue().stream()
                .map(ApiRecord::getUrl)
                .collect(Collectors.toList());

            MergedApi api = new MergedApi();
            api.setMergedUrl(entry.getKey());
            api.setOriginalUrls(toJson(urls));
            api.setMethod(entry.getValue().get(0).getMethod());
            api.setSourceCount(urls.size());
            api.setMergeType("DEFAULT");
            api.setCreateTime(now);
            result.add(api);
        }
        return result;
    }

    private String toJson(List<String> urls) {
        try {
            return objectMapper.writeValueAsString(urls);
        } catch (Exception e) {
            return "[]";
        }
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add src/main/java/com/audit/apimerge/pipeline/MergePipeline.java
git commit -m "feat: add merge pipeline main loop"
```

---

## Task 9: 启动入口

**Files:**
- Create: `src/main/java/com/audit/apimerge/App.java`

- [ ] **Step 1: 创建启动类**

```java
package com.audit.apimerge;

import com.audit.apimerge.config.AppConfig;
import com.audit.apimerge.config.ConfigLoader;
import com.audit.apimerge.pipeline.MergePipeline;

public class App {
    public static void main(String[] args) {
        String configPath = args.length > 0 ? args[0] : "config.yaml";
        AppConfig config = ConfigLoader.load(configPath);

        System.out.println("Starting API Merge V2.0...");
        MergePipeline pipeline = new MergePipeline(config);
        pipeline.run();
    }
}
```

- [ ] **Step 2: 添加运行配置（可选）**

```bash
# 运行命令
java -jar api-merge-1.0.0.jar config.yaml
```

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/audit/apimerge/App.java
git commit -m "feat: add app entry point"
```

---

## Task 10: 测试与验证

**Files:**
- Create: `src/test/java/com/audit/apimerge/pipeline/MergePipelineTest.java`

- [ ] **Step 1: 创建集成测试**

```java
package com.audit.apimerge.pipeline;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MergePipelineTest {
    @Test
    void shouldGroupByNormalizedUrl() {
        // 测试分组逻辑
    }
}
```

- [ ] **Step 2: 运行完整测试**

Run: `mvn test`
Expected: All tests pass

- [ ] **Step 3: 最终提交**

```bash
git add .
git commit -m "feat: complete V2.0 implementation"
```

---

## 完成检查

- [ ] 所有 Task 完成
- [ ] `mvn test` 通过
- [ ] 代码已提交