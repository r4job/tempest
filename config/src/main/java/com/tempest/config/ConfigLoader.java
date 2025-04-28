package com.tempest.config;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

public class ConfigLoader {
    public static WarmingConfig load(String fileName) {
        Yaml yaml = new Yaml();
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) throw new IllegalArgumentException("Config file not found: " + fileName);
            return yaml.loadAs(input, WarmingConfig.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config: " + e.getMessage(), e);
        }
    }
}
