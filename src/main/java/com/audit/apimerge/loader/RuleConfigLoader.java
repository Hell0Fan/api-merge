package com.audit.apimerge.loader;

import com.audit.apimerge.model.RuleConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
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
            String json = new String(Files.readAllBytes(Paths.get(configPath)));
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
