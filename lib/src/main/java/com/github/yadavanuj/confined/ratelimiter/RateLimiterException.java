package com.github.yadavanuj.confined.ratelimiter;

public class RateLimiterException extends RuntimeException {
    public RateLimiterException(Exception e) {
        super(e);
    }
}
