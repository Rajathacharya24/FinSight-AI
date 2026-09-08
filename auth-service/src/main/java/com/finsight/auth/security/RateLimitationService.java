package com.finsight.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimitationService {

    private final StringRedisTemplate redisTemplate;
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration BLOCK_TIME = Duration.ofMinutes(1);

    public boolean isRateLimited(String key) {
        String redisKey = "rate_limit:" + key;
        String countStr = redisTemplate.opsForValue().get(redisKey);
        if (countStr != null && Integer.parseInt(countStr) >= MAX_ATTEMPTS) {
            return true;
        }
        return false;
    }

    public void incrementAttempt(String key) {
        String redisKey = "rate_limit:" + key;
        Long attempts = redisTemplate.opsForValue().increment(redisKey);
        if (attempts != null && attempts == 1) {
            redisTemplate.expire(redisKey, BLOCK_TIME);
        }
    }

    public void resetAttempt(String key) {
        String redisKey = "rate_limit:" + key;
        redisTemplate.delete(redisKey);
    }
}
