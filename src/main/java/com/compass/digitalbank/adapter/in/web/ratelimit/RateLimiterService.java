package com.compass.digitalbank.adapter.in.web.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimiterService {

    private static final long SINGLE_TOKEN = 1L;
    private static final long MAX_TRACKED_CLIENTS = 100_000L;
    private static final Duration BUCKET_IDLE_TTL = Duration.ofMinutes(15);

    private final RateLimitProperties properties;
    private final Cache<String, Bucket> buckets;

    public RateLimiterService(RateLimitProperties properties) {
        this.properties = properties;
        this.buckets = Caffeine.newBuilder()
                .expireAfterAccess(BUCKET_IDLE_TTL)
                .maximumSize(MAX_TRACKED_CLIENTS)
                .build();
    }

    public boolean tryConsume(String clientKey) {
        if (!properties.enabled()) {
            return true;
        }
        return buckets.get(clientKey, key -> newBucket()).tryConsume(SINGLE_TOKEN);
    }

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(properties.capacity())
                .refillGreedy(properties.refillTokens(), Duration.ofSeconds(properties.refillPeriodSeconds()))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
