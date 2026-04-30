# API Merge V2.0 设计文档

## 背景

V1.0 是规则引擎，需要手动配置规则。V2.0 要实现自动化：
- 从 ClickHouse 读取 API 数据
- 自动识别需要合并的接口（内置规则 + 默认算法）
- 合并结果写入 MySQL
- 持续运行，断点续传

## 架构

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  ClickHouse    │────▶│  MergePipeline  │────▶│     MySQL       │
│  audit_record  │     │                  │     │  merged_api     │
└─────────────────┘     └─────────────────┘     └─────────────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │   ApiMergeEngine   │
                    │  ┌───────────────┐  │
                    │  │ 内置规则匹配   │  │
                    │  └───────────────┘  │
                    │  ┌───────────────┐  │
                    │  │ 默认参数化算法 │  │
                    │  └───────────────┘  │
                    └─────────────────────┘
```

## 核心流程

1. 读取 progress.txt 获取 last_processed_id
2. 查询 ClickHouse: `SELECT * FROM audit_record WHERE id > last_processed_id ORDER BY id`
3. 对每条 URL：
   - 先尝试内置规则匹配
   - 无匹配则使用默认参数化算法
   - 有合并结果则写入 MySQL
4. 更新 last_processed_id
5. 立即处理下一批

## 默认合并算法

### 标准化规则

将 URL 中的可变部分替换为占位符：

| 原始URL | 标准化后 |
|--------|----------|
| /api/user/123/detail | /api/user/{p}/detail |
| /api/user/abc/detail | /api/user/{p}/detail |
| /api/order/456/items | /api/order/{p}/items |

### 参数识别

以下模式视为参数：
- 纯数字：`123`、`456789`
- 字母数字混合：`abc123`、`uuid-type-1`
- 纯字母（长度>3）：`abcd`（可选）

### 归类逻辑

1. 对每个 URL 执行标准化
2. 相同标准化的 URL 归为一组
3. 取组内最短或最通用的路径作为 merged_url

## 数据模型

### ClickHouse 输入 (audit_record)

假设字段：
- id (Long)
- url (String)
- method (String)
- name (String)
- create_time (DateTime)

### MySQL 输出 (merged_api)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| merged_url | String | 合并后的标准化URL |
| original_urls | Text | 原始URL列表（JSON数组） |
| method | String | HTTP方法 |
| source_count | Int | 来源接口数量 |
| merge_type | String | RULE / DEFAULT |
| create_time | DateTime |

### 进度文件 (progress.txt)

```
last_processed_id=12345
```

## 配置

```yaml
clickhouse:
  jdbc-url: "jdbc:clickhouse://host:8123/database"
  username: "default"
  password: ""
  batch-size: 1000

mysql:
  jdbc-url: "jdbc:mysql://host:3306/database"
  username: ""
  password: ""
  table: "merged_api"

rule:
  config-path: "rules.json"

progress:
  file: "progress.txt"
```

## 错误处理

- ClickHouse 连接失败：等待重试
- MySQL 写入失败：记录日志，保留内存缓存，重试
- 无匹配URL：跳过，不写入
- 进程中断：下次启动从 last_processed_id 继续