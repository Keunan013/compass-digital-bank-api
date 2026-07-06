package com.compass.digitalbank.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.seed")
public record SeedProperties(
        boolean enabled,
        String defaultPassword
) {
}
