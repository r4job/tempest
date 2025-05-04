package com.tempest.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

public class ConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

    public static WarmingConfig load(String fileName) {
        Yaml yaml = new Yaml();
        try {
            InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream(fileName);
            if (input == null) {
                IllegalArgumentException e = new IllegalArgumentException("Config file not found: " + fileName);
                logger.error("[ConfigLoader]", e);
                throw e;
            }
            return yaml.loadAs(input, WarmingConfig.class);
        } catch (Exception e) {
            RuntimeException ex = new RuntimeException("Failed to load config: " + e.getMessage(), e);
            logger.error("[ConfigLoader]", ex);
            throw ex;
        }
    }
}
