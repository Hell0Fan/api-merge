# API合并规则引擎 V1.0 Release Notes

## 版本信息

- **版本号**: V1.0
- **发布日期**: 2026-04-29
- **仓库地址**: https://github.com/Hell0Fan/api-merge

---

## 功能特性

### 1. 多种规则类型支持
| 类型 | 标识 | 说明 |
|------|------|------|
| 路径参数合并 | `path_param` | 从URL路径中提取参数并合并 |
| 精确匹配 | `exact` | 路径完全匹配，不进行合并 |
| 路径前缀合并 | `prefix` | 相同前缀的路径统一合并 |
| 正则表达式合并 | `regex` | 支持正则表达式灵活匹配 |

### 2. 路径参数提取
- 支持位置匹配: `/api/user/*/detail` → `{p1}`
- 支持命名匹配: `/api/user/{userId}/detail` → `{userId}`

### 3. JSON配置文件管理
- 通过JSON文件定义合并规则
- 支持热加载

---

## 快速开始

### 1. 引入依赖

```xml
<dependency>
    <groupId>com.audit</groupId>
    <artifactId>api-merge</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 创建规则配置文件 `rules.json`

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
      "enabled": true
    }
  ]
}
```

### 3. 使用引擎

```java
ApiMergeEngine engine = new ApiMergeEngine("/path/to/rules.json");
MergeResult result = engine.merge("/api/user/123/detail");

// 结果
// mergedUrl: /api/user/{userId}/detail
// extractedParams: {userId=123, p1=123}
```

---

## 测试结果

| 测试项 | 结果 |
|--------|------|
| 功能测试 | 7/7 通过 |
| 性能测试 | 0.0068ms < 1ms ✓ |
| 规则类型支持 | 4种全部支持 |
| 命名参数匹配 | ✓ |

---

## 项目结构

```
api-merge/
├── pom.xml
├── src/
│   ├── main/java/com/audit/apimerge/
│   │   ├── ApiMergeEngine.java      # 主引擎
│   │   ├── model/                    # 数据模型
│   │   │   ├── MergeRule.java
│   │   │   ├── MergeResult.java
│   │   │   └── RuleConfig.java
│   │   ├── engine/                   # 规则匹配器
│   │   │   ├── RuleMatcher.java
│   │   │   ├── PathParamMatcher.java
│   │   │   ├── ExactMatcher.java
│   │   │   ├── PrefixMatcher.java
│   │   │   └── RegexMatcher.java
│   │   └── loader/
│   │       └── RuleConfigLoader.java # 配置加载器
│   └── test/resources/
│       └── test-rules.json           # 测试配置
└── docs/
    └── superpowers/
        ├── specs/                    # 需求规格说明
        └── plans/                    # 实现计划
```

---

## 规则属性说明

| 属性 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | String | 是 | 规则唯一标识 |
| `name` | String | 是 | 规则名称 |
| `type` | String | 是 | 规则类型 |
| `priority` | Integer | 是 | 优先级 (数字越小优先级越高) |
| `matchPattern` | String | 是 | 匹配模式 |
| `extractParams` | Array | 否 | 提取的参数名列表 |
| `outputPattern` | String | 是 | 合并后的输出路径 |
| `enabled` | Boolean | 是 | 是否启用 |

---

## 后续计划

- [ ] 热加载机制优化 (WatchService)
- [ ] 正则表达式Pattern缓存
- [ ] Matcher实例复用
- [ ] 管理界面

---

## 更新日志

### V1.0 (2026-04-29)
- 初始版本发布
- 支持4种规则类型
- 路径参数提取（位置匹配 + 命名匹配）
- JSON配置文件加载
- 性能 < 1ms