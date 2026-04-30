package com.audit.apimerge.pipeline;

import com.audit.apimerge.config.AppConfig;
import com.audit.apimerge.model.ApiRecord;
import com.audit.apimerge.model.MergedApi;
import com.audit.apimerge.reader.ClickHouseReader;
import com.audit.apimerge.writer.MySqlWriter;
import com.audit.apimerge.progress.ProgressManager;
import com.audit.apimerge.ApiMergeEngine;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 规范化后的API记录，包含原始记录、标准化URL和合并类型
 */
class NormalizedRecord {
    final String normalizedUrl;  // 标准化后的URL
    final String mergeType;       // 合并类型：RULE/DEFAULT/NONE
    final String app;             // 应用标识
    final String method;          // HTTP方法
    final ApiRecord record;       // 原始记录

    NormalizedRecord(String normalizedUrl, String mergeType, String app, String method, ApiRecord record) {
        this.normalizedUrl = normalizedUrl;
        this.mergeType = mergeType;
        this.app = app;
        this.method = method;
        this.record = record;
    }
}

/**
 * 主流程编排器
 * 负责从ClickHouse读取API数据，进行标准化和合并处理，然后写入MySQL
 */
public class MergePipeline {
    private final ClickHouseReader reader;           // ClickHouse读取器
    private final MySqlWriter writer;                // MySQL写入器
    private final ProgressManager progressManager;   // 断点续传管理器
    private final ApiMergeEngine engine;             // 内置规则引擎
    private final DefaultMerger defaultMerger;       // 默认合并算法
    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile boolean running = true;        // 运行标志

    /**
     * 构造函数，初始化所有组件
     * @param config 应用配置
     */
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

    /**
     * 启动主循环，持续处理数据
     * 流程：读取 → 标准化 → 合并 → 写入 → 更新进度 → 循环
     */
    public void run() {
        // 加载上次处理进度
        long lastProcessedId = progressManager.load();
        System.out.println("Starting from id: " + lastProcessedId);

        while (running) {
            try {
                // 从ClickHouse读取增量数据
                List<ApiRecord> records = reader.queryAfterId(lastProcessedId);

                // 无新数据时等待5秒后继续
                if (records.isEmpty()) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        break;
                    }
                    continue;
                }

                // 按标准化URL分组
                Map<String, List<NormalizedRecord>> grouped = groupByNormalized(records);

                // 构建合并结果（至少2条记录才合并）
                List<MergedApi> mergedApis = buildMergedApis(grouped);
                if (!mergedApis.isEmpty()) {
                    writer.write(mergedApis);
                }

                // 更新断点进度
                lastProcessedId = records.get(records.size() - 1).getId();
                progressManager.save(lastProcessedId);

                System.out.println("Processed " + records.size() + " records, progress: " + lastProcessedId);
            } catch (Exception e) {
                // 异常处理：记录错误并等待后重试
                System.err.println("Error processing batch: " + e.getMessage());
                e.printStackTrace();
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ie) {
                    break;
                }
            }
        }
    }

    /**
     * 停止主循环
     */
    public void stop() {
        running = false;
    }

    /**
     * 将记录按标准化URL分组（考虑应用和方法）
     * 分组键 = 应用 + HTTP方法 + 标准化URL
     * @param records 待处理的API记录列表
     * @return 分组后的Map
     */
    private Map<String, List<NormalizedRecord>> groupByNormalized(List<ApiRecord> records) {
        Map<String, List<NormalizedRecord>> grouped = new HashMap<>();
        for (ApiRecord record : records) {
            // 对每条记录进行标准化处理
            NormalizedRecord nr = normalize(record);
            // 分组键：应用_方法_标准化URL
            String groupKey = nr.app + "_" + nr.method + "_" + nr.normalizedUrl;
            grouped.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(nr);
        }
        return grouped;
    }

    /**
     * 标准化处理：优先使用内置规则，失败则使用默认算法
     * 同时记录应用标识和HTTP方法
     * @param record API记录
     * @return 规范化后的记录
     */
    private NormalizedRecord normalize(ApiRecord record) {
        String url = record.getUrl();
        String app = record.getApp() != null ? record.getApp() : "default";
        String method = record.getMethod() != null ? record.getMethod() : "UNKNOWN";

        // 优先尝试内置规则匹配
        com.audit.apimerge.model.MergeResult result = engine.merge(url);
        if (result.getMergedUrl() != null && !result.getMergedUrl().equals(url)) {
            return new NormalizedRecord(result.getMergedUrl(), "RULE", app, method, record);
        }
        // 无规则匹配时使用默认算法
        String defaultResult = defaultMerger.merge(url);
        String mergeType = defaultResult.equals(url) ? "NONE" : "DEFAULT";
        return new NormalizedRecord(defaultResult, mergeType, app, method, record);
    }

    /**
     * 构建合并结果
     * 只有同一应用+同一方法+同一标准化URL下有2条及以上记录时才生成合并结果
     * @param grouped 按标准化URL分组的记录
     * @return 合并后的API列表
     */
    private List<MergedApi> buildMergedApis(Map<String, List<NormalizedRecord>> grouped) {
        List<MergedApi> result = new ArrayList<>();
        long now = System.currentTimeMillis();

        for (Map.Entry<String, List<NormalizedRecord>> entry : grouped.entrySet()) {
            // 跳过记录数少于2的组（无需合并）
            if (entry.getValue().size() < 2) continue;

            // 获取分组中的代表性记录
            NormalizedRecord first = entry.getValue().get(0);

            // 提取原始URL列表
            List<String> urls = entry.getValue().stream()
                .map(nr -> nr.record.getUrl())
                .collect(Collectors.toList());

            // 获取合并类型
            String mergeType = first.mergeType;

            // 创建合并结果
            MergedApi api = new MergedApi();
            // mergedUrl 只包含标准化URL部分
            api.setMergedUrl(first.normalizedUrl);
            api.setApp(first.app);                        // 应用标识
            api.setOriginalUrls(toJson(urls));            // 原始URL列表（JSON）
            api.setMethod(first.method);                  // HTTP方法
            api.setSourceCount(urls.size());              // 来源记录数
            api.setMergeType(mergeType);                  // 合并类型
            api.setCreateTime(now);
            result.add(api);
        }
        return result;
    }

    /**
     * 将URL列表转换为JSON字符串
     * @param urls URL列表
     * @return JSON字符串，转换失败时返回空数组
     */
    private String toJson(List<String> urls) {
        try {
            return objectMapper.writeValueAsString(urls);
        } catch (Exception e) {
            return "[]";
        }
    }
}