package com.compass.digitalbank.adapter.out.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
        String secret,
        long expirationMinutes,
        String issuer
) {
}
