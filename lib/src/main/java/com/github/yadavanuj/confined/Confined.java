package com.github.yadavanuj.confined;

import com.github.yadavanuj.confined.bulkhead.BulkHead;
import com.github.yadavanuj.confined.bulkhead.BulkHeadConfig;
import com.github.yadavanuj.confined.bulkhead.BulkHeadRegistry;
import com.github.yadavanuj.confined.circuitbreaker.CircuitBreaker;
import com.github.yadavanuj.confined.circuitbreaker.CircuitBreakerConfig;
import com.github.yadavanuj.confined.circuitbreaker.CircuitBreakerRegistry;
import com.github.yadavanuj.confined.ratelimiter.RateLimiter;
import com.github.yadavanuj.confined.ratelimiter.RateLimiterConfig;
import com.github.yadavanuj.confined.ratelimiter.RateLimiterRegistry;

import java.util.HashMap;
import java.util.Map;

public interface Confined {
    Registry<BulkHead, BulkHeadConfig> register(BulkHeadConfig config);
    Registry<CircuitBreaker, CircuitBreakerConfig> register(CircuitBreakerConfig config);
    Registry<RateLimiter, RateLimiterConfig> register(RateLimiterConfig config);

    public class ConfinedImpl implements Confined {
        private Map<String, Registry<BulkHead, BulkHeadConfig>> bulkHeads = new HashMap<>();
        private Map<String, Registry<CircuitBreaker, CircuitBreakerConfig>> circuitBreakers = new HashMap<>();
        private Map<String, Registry<RateLimiter, RateLimiterConfig>> rateLimiters = new HashMap<>();
        public ConfinedImpl() {

        }

        @Override
        public Registry<BulkHead, BulkHeadConfig> register(BulkHeadConfig config) {
            final Registry<BulkHead, BulkHeadConfig> registry = new BulkHeadRegistry(config);
            this.bulkHeads.put(config.getKey(), registry);
            return registry;
        }

        @Override
        public Registry<CircuitBreaker, CircuitBreakerConfig> register(CircuitBreakerConfig config) {
            final Registry<CircuitBreaker, CircuitBreakerConfig> registry = new CircuitBreakerRegistry(config);
            this.circuitBreakers.put(config.getOperationName(), registry);
            return registry;
        }

        @Override
        public Registry<RateLimiter, RateLimiterConfig> register(RateLimiterConfig config) {
            final Registry<RateLimiter, RateLimiterConfig> registry = new RateLimiterRegistry(config);
            this.rateLimiters.put(config.getOperationName(), registry);
            return registry;
        }
    }
}
