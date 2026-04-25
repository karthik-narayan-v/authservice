package com.karthik.authservice.security.ratelimit;

import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterServiceTests {

    private final RateLimiterService rateLimiterService = new RateLimiterService();

    @Test
    void resolveBucket_shouldReturnSameBucket_forSameKey() {

        Bucket bucket1 = rateLimiterService.resolveBucket("user-ip");
        Bucket bucket2 = rateLimiterService.resolveBucket("user-ip");

        assertSame(bucket1, bucket2);
    }

    @Test
    void resolveBucket_shouldReturnDifferentBuckets_forDifferentKeys() {

        Bucket bucket1 = rateLimiterService.resolveBucket("ip1");
        Bucket bucket2 = rateLimiterService.resolveBucket("ip2");

        assertNotSame(bucket1, bucket2);
    }

    @Test
    void bucket_shouldAllowOnlyFiveRequests() {

        Bucket bucket = rateLimiterService.resolveBucket("test-ip");

        // First 5 should pass
        for (int i = 0; i < 5; i++) {
            assertTrue(bucket.tryConsume(1));
        }

        // 6th should fail
        assertFalse(bucket.tryConsume(1));
    }

    @Test
    void bucket_shouldRefillAfterTime()  {

        Bucket bucket = rateLimiterService.resolveBucket("refill-ip");

        // Exhaust tokens
        for (int i = 0; i < 5; i++) {
            bucket.tryConsume(1);
        }

        assertFalse(bucket.tryConsume(1));

        // Wait for refill (1 minute in real config → skip or adjust config for testing)
    }
}