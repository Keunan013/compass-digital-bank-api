package com.compass.digitalbank.adapter.in.web.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(
        boolean enabled,
        long capacity,
        long refillTokens,
        long refillPeriodSeconds
) {
}
