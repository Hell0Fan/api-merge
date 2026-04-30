package com.audit.apimerge;

import com.audit.apimerge.config.AppConfig;
import com.audit.apimerge.config.ConfigLoader;
import com.audit.apimerge.pipeline.MergePipeline;

/**
 * 应用入口
 * 用法：java -jar api-merge.jar [config.yaml]
 * 默认配置文件：config.yaml
 */
public class App {
    public static void main(String[] args) {
        // 获取配置文件路径（默认 config.yaml）
        String configPath = args.length > 0 ? args[0] : "config.yaml";

        // 加载配置
        AppConfig config = ConfigLoader.load(configPath);

        // 启动处理流程
        System.out.println("Starting API Merge V2.0...");
        MergePipeline pipeline = new MergePipeline(config);
        pipeline.run();
    }
}