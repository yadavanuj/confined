package com.github.yadavanuj.confined.internal.permits.ratelimiter;

public class RateLimiterException extends RuntimeException {
    public RateLimiterException(Exception e) {
        super(e);
    }
}
