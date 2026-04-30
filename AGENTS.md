# api-merge V2.0

Java API 合并规则引擎 (Maven 项目)

## 构建与测试

```bash
mvn clean compile    # 编译
mvn test             # 运行测试
mvn test -Dtest=TestClassName  # 运行单个测试类
mvn package          # 打包
```

## 环境要求

- JDK 1.8+
- Maven 3.5.3+
- MySQL (结果写入)
- ClickHouse (API数据源)

## V2.0 入口类

- `com.audit.apimerge.pipeline.MergePipeline` - V2.0主入口
- 配置文件: `config.yaml` (根目录)

## 核心组件

| 组件 | 说明 |
|------|------|
| `ClickHouseReader` | 从ClickHouse读取API数据 |
| `MySqlWriter` | 将合并结果写入MySQL |
| `DefaultMerger` | 默认合并算法 |
| `ProgressManager` | 断点续传管理 |
| `MergePipeline` | 完整处理流程 |

## 合并逻辑

- 分组键: `app + method + normalizedUrl`
- 应用标识: 从URL路径提取 (如 `/api/shop/*` → `shop`)
- 支持连续运行: 启用checkpoint后自动续传

## 项目结构

```
src/main/java/com/audit/apimerge/
├── pipeline/           # V2.0处理流程
│   ├── MergePipeline.java
│   ├── MergeStrategy.java
│   └── DefaultMerger.java
├── reader/             # 数据读取
│   └── ClickHouseReader.java
├── writer/             # 数据写入
│   └── MySqlWriter.java
├── progress/           # 断点管理
│   └── ProgressManager.java
├── config/             # 配置加载
│   ├── AppConfig.java
│   └── ConfigLoader.java
└── model/              # 数据模型
    ├── ApiRecord.java
    └── MergedApi.java
```

## 测试

```bash
mvn test                       # 运行全部25个测试
mvn test -Dtest=ComplexMergeTest  # 复杂合并场景
```