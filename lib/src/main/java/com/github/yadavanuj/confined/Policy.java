package com.github.yadavanuj.confined;

public interface Policy {
    PolicyType getPolicyType();
    boolean acquire();
    void release();

    public static enum PolicyType {
        BulkHead,
        CircuitBreaker,
        RateLimiter;
    }
}
