# API合并规则需求规格说明

## 1. 概述

### 1.1 背景

在API审计系统中，不同应用的同一业务接口可能以不同路径参数形式存在，例如：
- `/api/user/1001/detail`
- `/api/user/1002/detail`

这些接口虽然业务相同，但路径中的参数不同，导致无法统一统计分析。API合并规则用于将这类路径统一归并，形成可统计的聚合接口。

### 1.2 目标

- 支持多种合并规则类型，满足不同业务场景
- 提供灵活的参数提取能力
- 通过JSON配置文件管理规则

---

## 2. 功能需求

### 2.1 规则类型

系统支持以下几种合并规则类型：

| 类型 | 标识 | 说明 | 示例 |
|------|------|------|------|
| 路径参数合并 | `path_param` | 从URL路径中提取参数并合并 | `/api/user/123/detail` → `/api/user/{userId}/detail` |
| 精确匹配 | `exact` | 路径完全匹配，不进行合并 | `/api/health` → `/api/health` |
| 路径前缀合并 | `prefix` | 相同前缀的路径统一合并 | `/api/internal/xxx` → `/api/internal` |
| 正则表达式合并 | `regex` | 支持正则表达式灵活匹配 | `/api/order/(\d+)/detail` → `/api/order/{orderId}/detail` |

### 2.2 规则属性

每个规则包含以下属性：

| 属性 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | String | 是 | 规则唯一标识，全局唯一 |
| `name` | String | 是 | 规则名称，便于识别 |
| `type` | String | 是 | 规则类型：`path_param`、`exact`、`prefix`、`regex` |
| `priority` | Integer | 是 | 优先级，数字越小优先级越高（1-100） |
| `matchPattern` | String | 是 | 匹配模式，用于匹配原始URL |
| `extractParams` | Array | 否 | 需要提取的参数名列表（仅path_param/regex使用） |
| `outputPattern` | String | 是 | 合并后的输出路径模板 |
| `enabled` | Boolean | 是 | 是否启用该规则 |
| `description` | String | 否 | 规则描述 |

### 2.3 规则执行逻辑

1. 按`priority`升序遍历所有启用的规则
2. 使用当前规则的`matchPattern`匹配请求URL
3. 匹配成功时，使用`extractParams`提取参数，应用`outputPattern`生成合并后的URL
4. 匹配失败时，继续尝试下一个规则
5. 所有规则都未匹配时，使用原始URL

### 2.4 路径参数提取

#### 2.4.1 提取模式

路径参数提取支持两种模式：

| 模式 | 说明 | 示例 |
|------|------|------|
| 位置匹配 | 按`*`位置对应提取 | `/api/user/*/detail` 提取为 `/api/user/{p1}/detail` |
| 命名匹配 | 按`{}`中的名称提取 | `/api/user/{userId}/detail` 提取为 `/api/user/{userId}/detail` |

#### 2.4.2 参数命名

- **位置参数**: 使用 `{p1}`, `{p2}`, `{p3}` ... 顺序命名
- **命名参数**: 使用 `{paramName}` 自定义命名

### 2.5 规则管理

#### 2.5.1 规则配置格式

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
      "name": "商品分类接口合并",
      "type": "path_param",
      "priority": 3,
      "matchPattern": "/api/product/category/*/list",
      "extractParams": ["categoryId"],
      "outputPattern": "/api/product/category/{categoryId}/list",
      "enabled": true,
      "description": "合并商品分类列表接口"
    },
    {
      "id": "rule-004",
      "name": "健康检查接口",
      "type": "exact",
      "priority": 10,
      "matchPattern": "/api/health",
      "outputPattern": "/api/health",
      "enabled": true,
      "description": "精确匹配不合并"
    },
    {
      "id": "rule-005",
      "name": "内部接口合并",
      "type": "prefix",
      "priority": 20,
      "matchPattern": "/api/internal/",
      "outputPattern": "/api/internal",
      "enabled": false,
      "description": "合并内部接口前缀（示例，未启用）"
    },
    {
      "id": "rule-006",
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

#### 2.5.2 规则加载

- 规则通过JSON配置文件定义
- 配置文件路径可通过配置指定
- 配置文件变更后自动热加载

---

## 3. 数据模型

### 3.1 合并结果数据结构

```json
{
  "mergedUrl": "/api/user/{userId}/detail",
  "originalUrl": "/api/user/1001/detail",
  "matchedRule": {
    "id": "rule-001",
    "name": "用户接口合并",
    "type": "path_param"
  },
  "extractedParams": {
    "userId": "1001",
    "p1": "1001"
  }
}
```

### 3.2 维度数据

提取的参数作为维度数据存储，用于后续统计分析：

```json
{
  "dimensions": {
    "userId": "1001",
    "orderId": null,
    "categoryId": null
  }
}
```

---

## 4. 非功能需求

### 4.1 性能要求

- 规则匹配耗时 < 1ms
- 支持规则数量 >= 1000条

### 4.2 可用性

- 支持规则配置文件热加载
- 配置文件变更后自动重新加载

### 4.3 扩展性

- 支持扩展新的规则类型（插件机制）
- 支持自定义参数转换逻辑

---

## 5. 场景示例

### 5.1 场景一：用户服务接口合并

| 原始URL | 合并后URL |
|---------|-----------|
| `/api/user/1001/detail` | `/api/user/{userId}/detail` |
| `/api/user/1002/detail` | `/api/user/{userId}/detail` |
| `/api/user/1003/profile` | `/api/user/{userId}/profile` |

### 5.2 场景二：订单服务接口合并

| 原始URL | 合并后URL |
|---------|-----------|
| `/api/order/5001/info` | `/api/order/{orderId}/info` |
| `/api/order/5002/info` | `/api/order/{orderId}/info` |
| `/api/order/5001/detail` | `/api/order/{orderId}/detail` |

### 5.3 场景三：混合场景

| 原始URL | 合并后URL | 说明 |
|---------|-----------|------|
| `/api/product/2001/info` | `/api/product/{productId}/info` | 商品信息 |
| `/api/product/2001/review` | `/api/product/{productId}/review` | 商品评价 |
| `/api/search?keyword=phone` | `/api/search?keyword=phone` | 搜索接口不合并（query参数） |
| `/api/file/upload` | `/api/file/upload` | 文件上传不合并 |

